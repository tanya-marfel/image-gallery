package com.epam.aws.controller;

import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.UnsubscribeResult;
import com.epam.aws.model.SubscriptionForm;
import com.epam.aws.service.SnsSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class SubscriptionController {

    private static final String SUBSCRIPTION_SUCCESS_VIEW = "subscription-success";
    private static final String ACTION_ATTRIBUTE = "action";

    @Autowired
    private SnsSubscriptionService subscriptionService;

    @Value("${amazon.s3.bucket-name}")
    private String bucketName;

    @PostMapping("/subscribe")
    public RedirectView subscribe(@ModelAttribute SubscriptionForm subscriptionForm,
                                  final RedirectAttributes redirectAttributes) {
        SubscribeResult result = subscriptionService.createSubscription(subscriptionForm.getEmail());
        if (result.getSubscriptionArn() == null) {
            redirectAttributes.addFlashAttribute(ACTION_ATTRIBUTE, "subscribe-error");
        } else {
            redirectAttributes.addFlashAttribute(ACTION_ATTRIBUTE, "subscribe-success");
        }
        redirectAttributes.addFlashAttribute("bucketName", bucketName);
        return new RedirectView(SUBSCRIPTION_SUCCESS_VIEW, true);
    }

    @GetMapping("/subscribe")
    public ModelAndView subscribe() {
        ModelAndView modelAndView = new ModelAndView("subscription");
        modelAndView.addObject("unsubscribe", "false");
        return modelAndView;
    }

    @GetMapping("/unsubscribe")
    public ModelAndView unsubscribe() {
        ModelAndView modelAndView = new ModelAndView("subscription");
        modelAndView.addObject("unsubscribe", "true");
        return modelAndView;
    }

    @PostMapping("/unsubscribe")
    public RedirectView unsubscribe(@ModelAttribute SubscriptionForm subscriptionForm,
                                    final RedirectAttributes redirectAttributes) {
        List<UnsubscribeResult> results =
                subscriptionService.unsubscribeFromTopic(subscriptionForm.getEmail(), bucketName);
        if (results.size() > 0) {
            redirectAttributes.addFlashAttribute(ACTION_ATTRIBUTE, "unsubscribe-success");
        } else {
            redirectAttributes.addFlashAttribute(ACTION_ATTRIBUTE, "unsubscribe-error");
        }
        redirectAttributes.addFlashAttribute("bucketName", bucketName);
        return new RedirectView(SUBSCRIPTION_SUCCESS_VIEW, true);
    }

    @GetMapping("/subscription-success")
    public String getSuccess(HttpServletRequest request) {
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            return SUBSCRIPTION_SUCCESS_VIEW;
        } else {
            return "redirect:/subscribe";
        }
    }
}
