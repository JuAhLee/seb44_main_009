package com.main.MainProject.member.controller;


import com.main.MainProject.auth.dto.LoginDto;
import com.main.MainProject.dto.SingleResponseDto;
import com.main.MainProject.member.dto.MemberPatchDto;
import com.main.MainProject.member.dto.MemberSignUpDto;
import com.main.MainProject.member.entity.Member;
import com.main.MainProject.member.mapper.MemberMapper;
import com.main.MainProject.member.service.MemberService;
import com.main.MainProject.dto.ListResponseDto;
import com.main.MainProject.util.UriCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/members")
@Validated
@Slf4j
public class MemberController {
    private final static String MEMBER_DEFAULT_URL = "/members";
    private final MemberService memberService;
    private final MemberMapper mapper;

    public MemberController(MemberService memberService, MemberMapper mapper) {
        this.memberService = memberService;
        this.mapper = mapper;
    }


    @PostMapping("/signup")
    public ResponseEntity signupMember(@Valid @RequestBody MemberSignUpDto memberSignUpDto){
        Member member = memberService.createMember(mapper.memberSignUpToMember(memberSignUpDto));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping() //securityConfiguration 통해 경로 변경 >  /auth/login
    public ResponseEntity loginForm(@Valid @RequestBody LoginDto loginDto)
    {
        Member member = mapper.loginDtoToMember(loginDto);

        Member createMember = memberService.createMember(member);
        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, createMember.getMemberId());
        return ResponseEntity.created(location).build();
    }

    @PostMapping("/logout")
    public ResponseEntity logoutMember(){
        return null;
    }

    @PatchMapping("/{member-id}")
    public ResponseEntity patchMember(@RequestHeader(value = "Authorization") String token,
                                    @Valid @RequestBody MemberPatchDto memberPatchDto){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Member member = memberService.updateMember(
                mapper.memberPatchDtoToMember(memberPatchDto),
                auth.getPrincipal().toString());

        return new ResponseEntity<>(
                new SingleResponseDto<>(mapper.memberToMemberResponseDto(member)), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{member-id}")
    public ResponseEntity getMember(@PathVariable("member-id") @Positive long memberId){
        Member member = memberService.findVerifiedMember(memberId);
        return new ResponseEntity<>(
                new SingleResponseDto<>(mapper.memberToMemberResponseDto(member))
                , HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getMembers(){
        List<Member> members = memberService.findMembers();
        return new ResponseEntity<>(
                new ListResponseDto<>(
                        mapper.memberToMemberResponses(members)),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{member-id}")
    public ResponseEntity deleteMember(@PathVariable("member-id") @Positive long memberId){
        memberService.deleteMember(memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
