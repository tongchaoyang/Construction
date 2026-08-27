package com.landmarkconstruction.backend.service;

import com.landmarkconstruction.backend.model.Project;
import com.landmarkconstruction.backend.model.QuoteRequest;
import com.landmarkconstruction.backend.model.Review;
import com.landmarkconstruction.backend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.notification.business-email}")
    private String businessEmail;

    @Value("${app.notification.from-address}")
    private String fromAddress;

    @Async("emailTaskExecutor")
    public void sendQuoteNotificationToBusiness(QuoteRequest request) {
        String body = """
                A new quote request has been submitted on the Landmark Construction & ADU website.

                Name: %s
                Email: %s
                Phone: %s
                Category: %s
                Description: %s
                Photos attached: %d

                View and manage this request in the admin dashboard.
                """.formatted(request.getName(), request.getEmail(), request.getPhone(),
                request.getCategory(), request.getDescription(), request.getImages().size());

        sendSafely(businessEmail,
                "New Quote Request from " + request.getName() + " — " + request.getCategory(),
                body);
    }

    @Async("emailTaskExecutor")
    public void sendQuoteConfirmationToCustomer(QuoteRequest request) {
        String body = """
                Hi %s,

                Thank you for reaching out to Landmark Construction & ADU (里程建筑). We received your request for:

                %s

                Our team will review the details and contact you shortly at %s.

                Landmark Construction & ADU
                97 E Brokaw Rd, STE 110, San Jose, CA 95112
                Phone: 408-915-9235
                Email: landmarkadu@gmail.com
                CSLB License #1140588
                """.formatted(request.getName(), request.getDescription(), request.getPhone());

        sendSafely(request.getEmail(),
                "We received your request — Landmark Construction & ADU",
                body);
    }

    @Async("emailTaskExecutor")
    public void sendReviewConfirmationToCustomer(User user, Project project, Review review) {
        String body = """
                Hi %s,

                Thank you for sharing your feedback on "%s" (%s). Your rating and comment have been posted \
                on the Landmark Construction & ADU website.

                Rating: %d/5
                Comment: %s

                We appreciate you taking the time to share your experience.

                Landmark Construction & ADU
                97 E Brokaw Rd, STE 110, San Jose, CA 95112
                Phone: 408-915-9235
                Email: landmarkadu@gmail.com
                CSLB License #1140588
                """.formatted(user.getFullName(), project.getTitleEn(), project.getTitleZh(),
                review.getRating(), review.getComment() != null ? review.getComment() : "(no comment)");

        sendSafely(user.getEmail(),
                "Thanks for your review — Landmark Construction & ADU",
                body);
    }

    private void sendSafely(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
