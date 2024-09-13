package ex.grpcserver.domain.reviews.service;

import bookstore.Bookstore;
import bookstore.ReviewServiceGrpc;
import ex.grpcserver.domain.reviews.entity.Review;
import ex.grpcserver.global.interceptor.AccessLoggingInterceptor;
import ex.grpcserver.global.interceptor.BasicAuthInterceptor;
import ex.grpcserver.global.utils.TimestampConverter;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService(interceptors = { AccessLoggingInterceptor.class, BasicAuthInterceptor.class })
public class ReviewGrpcService extends ReviewServiceGrpc.ReviewServiceImplBase {
    private final ReviewService reviewService;

    @Autowired
    public ReviewGrpcService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Override
    public void getReviews(Bookstore.GetReviewsRequest request, StreamObserver<Bookstore.Review> responseObserver) {
        List<Review> reviews = reviewService.findByBookId(request.getBookId());

        for (Review review : reviews) {
            responseObserver.onNext(
                    Bookstore.Review.newBuilder()
                            .setId(review.getId())
                            .setBookId(review.getBook().getId())
                            .setRating(review.getRating())
                            .setCreatedDate(TimestampConverter.toProto(review.getCreatedDate()))
                            .setContent(review.getContent())
                            .build()
            );
        }

        responseObserver.onCompleted();
    }
}
