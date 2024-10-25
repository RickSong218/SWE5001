package com.ssms.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ssms.common.config.SSMSProps;
import com.ssms.props.AppProps;

@Component
public class PageFactory {

    @Autowired
    SSMSProps SSMSProps;

    @Autowired
    AppProps appProps;

    public Page buildHomePage() {
        return Page.builder()
                .title("SSMS - Online Scheduling Software")
                .description("SSMS is a web application that helps small businesses create schedules online and automatically communicate them via text message with hourly workers.")
                .templateName("home")
                .version(SSMSProps.getDeployEnv())
                .build();
    }

    public Page buildAboutPage() {
        return Page.builder()
                .title("About SSMS")
                .description("Learn about the members of the SSMS team and the origin of the company.")
                .templateName("about")
                .version(SSMSProps.getDeployEnv())
                .build();
    }

    public Page buildCareersPage() {
        return Page.builder()
                .title("SSMS Careers")
                .description("If you’re looking to improve the way small businesses schedule their hourly workers, you are invited to apply to join our team in San Francisco.")
                .templateName("careers")
                .cssId("careers")
                .version(SSMSProps.getDeployEnv())
                .build();
    }

    public Page buildPricingPage() {
        return Page.builder()
                .title("SSMS Pricing")
                .description("SSMS’s software pricing is affordable for any size team. There is a monthly subscription based on the number of employees your company has.")
                .templateName("pricing")
                .version(SSMSProps.getDeployEnv())
                .build();
    }

    public Page buildPrivacyPolicyPage() {
        return Page.builder()
                .title("SSMS Privacy Policy")
                .description("SSMS’s Privacy Policy will walk you through through security protocols, data storage, and legal compliance that all clients need to know.")
                .templateName("privacypolicy")
                .version(SSMSProps.getDeployEnv())
                .build();
    }

    public Page buildSignupPage() {
        return Page.builder()
                .title("SSMS Privacy Policy")
                .description("Sign Up for Your 30 Day Free SSMS Trial\", Description: \"Sign up for a 30 day free trial of SSMS today to create your schedule online. We’ll distribute it to your team using automated text messages.")
                .templateName("signup")
                .cssId("sign-up")
                .version(SSMSProps.getDeployEnv())
                .build();
    }

    public Page buildEarlyPage() {
        return Page.builder()
                .title("Early Access Signup")
                .description("Get early access for SSMS")
                .templateName("early")
                .cssId("sign-up")
                .version(SSMSProps.getDeployEnv())
                .build();
    }


    public Page buildTermsPage() {
        return Page.builder()
                .title("SSMS Terms and Conditions")
                .description("SSMS’s Terms and Conditions point out the liability, disclaimers, exclusions, and more that all users of our website must agree to.")
                .templateName("terms")
                .version(SSMSProps.getDeployEnv())
                .build();
    }

    public Page buildConfirmPage() {
        return Page.builder()
                .title("Open your email and click on the confirmation link!")
                .description("Check your email and click the link for next steps")
                .templateName("confirm")
                .cssId("confirm")
                .version(SSMSProps.getDeployEnv())
                .build();
    }

    public Page buildNewCompanyPage() {
        return Page.builder()
                .title("Create a new company")
                .description("Get started with a new SSMS account")
                .templateName("new_company")
                .cssId("newCompany")
                .version(SSMSProps.getDeployEnv())
                .build();
    }

    // lombok inheritance workaround, details here: https://www.baeldung.com/lombok-builder-inheritance
    public LoginPage buildLoginPage() {
        return LoginPage.childBuilder()
                .title("SSMS Log in")
                .description("Log in to SSMS to start scheduling your workers. All you’ll need is your email and password.")
                .templateName("login")
                .cssId("login")
                .version(SSMSProps.getDeployEnv())
                .build();
    }

    public ActivatePage buildActivatePage() {
        return ActivatePage.childBuilder()
                .title("Activate your SSMS account")
                .templateName("activate")
                .cssId("sign-up")
                .version(SSMSProps.getDeployEnv())
                .build();
    }

    public ResetPage buildResetPage() {
        return ResetPage.childBuilder()
                .title("Password Reset")
                .cssId("sign-up")
                .templateName("reset")
                .description("Reset the password for your SSMS account.")
                .recaptchaPublic(appProps.getRecaptchaPublic())
                .version(SSMSProps.getDeployEnv())
                .build();
    }

    public Page buildResetConfirmPage() {
        return Page.builder()
                .title("Please check your email for a reset link!")
                .description("Check your email and click the link for next steps")
                .templateName("confirm")
                .cssId("confirm")
                .version(SSMSProps.getDeployEnv())
                .build();

    }

    public ConfirmResetPage buildConfirmResetPage() {
        return ConfirmResetPage.childBuilder()
                .title("Reset your SSMS password")
                .description("Follow steps to reset your SSMS password")
                .cssId("sign-up")
                .templateName("confirmreset")
                .version(SSMSProps.getDeployEnv())
                .build();
    }
}