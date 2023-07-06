package com.main.MainProject.review;

import com.main.MainProject.exception.BusinessLogicException;
import com.main.MainProject.exception.ExceptionCode;
import com.main.MainProject.member.entity.Member;
import com.main.MainProject.member.service.MemberService;
import com.main.MainProject.order.entity.Order;
import com.main.MainProject.order.entity.OrderProduct;
import com.main.MainProject.order.service.OrderService;
import com.main.MainProject.product.entity.Product;
import com.main.MainProject.product.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final MemberService memberService;
    private final ProductService productService;

    private final OrderService orderService;

    public ReviewService(ReviewRepository reviewRepository, MemberService memberService, ProductService productService, OrderService orderService) {
        this.reviewRepository = reviewRepository;
        this.memberService = memberService;
        this.productService = productService;
        this.orderService = orderService;
    }

    //리뷰 생성
    public Review createReview(Review review, long orderId, long productId, long memberId){
        Order findOrder = orderService.findVerficatedOrder(orderId);
        Member findMember = memberService.findVerifiedMember(memberId);
        Product findProduct = productService.findVerifiedProduct(productId);

        orderService.isOrderByMember(findOrder, findMember);
        OrderProduct findOrderProduct = orderService.findOrderProduct(findOrder, findProduct);

        //현재 리뷰 작성 가능인지 확인
        if(findOrderProduct.getReviewstatus() != OrderProduct.Reviewstatus.POSSIBLE_REVIEW){
            new BusinessLogicException(ExceptionCode.CAN_NOT_WRITE_REVIEW);
        }

        review.setMember(findMember);
        review.setProduct(findProduct);
        findOrderProduct.setReviewstatus(OrderProduct.Reviewstatus.REVIEW_WIITE);

        return reviewRepository.save(review);
    }

    //리뷰 수정
    public Review updateReview(long reviewId, long memberId, Review review){
        Member findMember = memberService.findVerifiedMember(memberId);
        Review findReview = findVerifiedReview(reviewId);

        if (findMember != findReview.getMember()){
            new BusinessLogicException(ExceptionCode.YOU_ARE_NOT_WRITER);
        }

        Optional.ofNullable(review.getTitle())
                .ifPresent(title ->findReview.setTitle(title));
        Optional.ofNullable(review.getContent())
                .ifPresent(content ->findReview.setContent(content));
        Optional.ofNullable(review.getScore())
                .ifPresent(score ->findReview.setScore(score));

        return reviewRepository.save(findReview);
    }

    //개별 리뷰 조회
    public Review getReview(long reviewId){
        return findVerifiedReview(reviewId);
    }

    //상품에 달린 모든 리뷰 조화
    public List<Review> getAllReviewsByProduct(long productId){
        Product findProduct = productService.findVerifiedProduct(productId);
        return reviewRepository.findAllByProduct(findProduct);
    }

    //회원이 작성한 모든 리뷰 조회
    public List<Review> getAllReviewsByMember(long memberId){
        Member findMember = memberService.findVerifiedMember(memberId);
        return reviewRepository.findAllByMember(findMember);
    }

    //리뷰 삭제
    public void deleteReview(long reviewId, long memberId){
        Review findReview = findVerifiedReview(reviewId);
        Member findMember = memberService.findVerifiedMember(memberId);

        if (findMember != findReview.getMember()){
            new BusinessLogicException(ExceptionCode.YOU_ARE_NOT_WRITER);
        }

        reviewRepository.delete(findReview);
    }

    //존재하는 리뷰인지 확인
    private Review findVerifiedReview(long reviewId){
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        Review findReview = optionalReview.orElseThrow(()->
                new BusinessLogicException(ExceptionCode.REVIEW_NOT_FOUND));
        return findReview;
    }

}
