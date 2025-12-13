# OneSignal Email Templates for MabsPlace

This document contains all email templates to be created in the OneSignal dashboard.

**OneSignal App ID:** `079f270c-81e2-43f8-8a51-266c69b6fc8a`
**Domain:** `mabsplace.app`

---

## Setup Instructions

1. Log in to OneSignal Dashboard: https://dashboard.onesignal.com/
2. Select app: **MabsPlace**
3. Navigate to: **Messages** → **Templates** → **Email Templates**
4. For each template below:
   - Click "New Email Template"
   - Set Template Name (must match exactly)
   - Copy/paste the Subject and HTML Body
   - Save the template

---

## Template 1: Email Verification Code

**Template Name:** `verification-code`
**Subject Line:** `Your MabsPlace Verification Code`
**Variables:** `code`, `expiration_minutes`

**HTML Body:**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verification Code</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Poppins', Arial, sans-serif;
            background-color: #f4f7fa;
            color: #333;
            line-height: 1.6;
        }

        .email-wrapper {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        }

        .email-header {
            background: linear-gradient(135deg, #1a73e8, #0d47a1);
            padding: 30px 20px;
            text-align: center;
        }

        .logo img {
            height: 40px;
            margin-bottom: 15px;
        }

        .header-title {
            color: #ffffff;
            font-size: 24px;
            font-weight: 600;
            margin: 0;
        }

        .email-body {
            padding: 40px 30px;
            color: #4a4a4a;
        }

        .greeting {
            font-size: 18px;
            font-weight: 500;
            margin-bottom: 20px;
        }

        .content-block {
            margin-bottom: 25px;
        }

        .content-block p {
            margin-bottom: 15px;
            font-size: 16px;
        }

        .verification-code {
            background: linear-gradient(135deg, #1a73e8, #0d47a1);
            color: #ffffff;
            font-size: 32px;
            font-weight: 700;
            letter-spacing: 8px;
            text-align: center;
            padding: 20px;
            margin: 30px 0;
            border-radius: 8px;
            font-family: 'Courier New', monospace;
        }

        .highlight-box {
            background-color: #fff3cd;
            border-left: 4px solid #ffc107;
            padding: 15px;
            margin: 20px 0;
            border-radius: 4px;
            font-size: 14px;
        }

        .email-footer {
            background-color: #f8f9fa;
            padding: 25px 30px;
            text-align: center;
            color: #757575;
            font-size: 14px;
            border-top: 1px solid #e0e0e0;
        }

        .footer-links {
            margin: 15px 0;
        }

        .footer-links a {
            color: #1a73e8;
            text-decoration: none;
            margin: 0 10px;
        }

        @media only screen and (max-width: 600px) {
            .email-wrapper {
                border-radius: 0;
            }
            .email-body {
                padding: 30px 20px;
            }
            .verification-code {
                font-size: 28px;
                letter-spacing: 6px;
            }
        }
    </style>
</head>
<body>
    <div class="email-wrapper">
        <div class="email-header">
            <div class="logo">
                <img src="https://admin.mabsplace.com/_next/static/media/mabsplace_light.55f402f2.png" alt="MabsPlace">
            </div>
            <h1 class="header-title">Email Verification</h1>
        </div>

        <div class="email-body">
            <div class="greeting">Hello!</div>
            <div class="content-block">
                <p>Thank you for registering with MabsPlace. To complete your email verification, please use the code below:</p>

                <div class="verification-code">
                    {{code}}
                </div>

                <p style="text-align: center; color: #757575; font-size: 14px;">
                    This code will expire in {{expiration_minutes}} minutes.
                </p>

                <div class="highlight-box">
                    <strong>⚠️ Security Notice:</strong> If you didn't request this code, please ignore this email. Never share this code with anyone.
                </div>
            </div>
        </div>

        <div class="email-footer">
            <div class="footer-links">
                <a href="https://mabsplace.app/privacy">Privacy Policy</a>
                <a href="https://mabsplace.app/terms">Terms of Service</a>
                <a href="https://mabsplace.app/contact">Contact Support</a>
            </div>
            <div style="margin-top: 15px;">
                © 2025 MabsPlace. All rights reserved.
            </div>
        </div>
    </div>
</body>
</html>
```

---

## Template 2: Subscription Renewed

**Template Name:** `subscription-renewed`
**Subject Line:** `Your {{service_name}} Subscription Has Been Renewed! 🎉`
**Variables:** `username`, `service_name`, `profile_name`, `new_end_date`

**HTML Body:**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Subscription Renewed</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Poppins', Arial, sans-serif;
            background-color: #f4f7fa;
            color: #333;
            line-height: 1.6;
        }

        .email-wrapper {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        }

        .email-header {
            background: linear-gradient(135deg, #10b981, #059669);
            padding: 30px 20px;
            text-align: center;
        }

        .logo img {
            height: 40px;
            margin-bottom: 15px;
        }

        .header-title {
            color: #ffffff;
            font-size: 24px;
            font-weight: 600;
            margin: 0;
        }

        .email-body {
            padding: 40px 30px;
            color: #4a4a4a;
        }

        .greeting {
            font-size: 18px;
            font-weight: 500;
            margin-bottom: 20px;
        }

        .content-block {
            margin-bottom: 25px;
        }

        .content-block p {
            margin-bottom: 15px;
            font-size: 16px;
        }

        .info-box {
            background-color: #f0fdf4;
            border-left: 4px solid #10b981;
            padding: 20px;
            margin: 25px 0;
            border-radius: 4px;
        }

        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid #d1fae5;
        }

        .info-row:last-child {
            border-bottom: none;
        }

        .info-label {
            font-weight: 500;
            color: #666;
        }

        .info-value {
            font-weight: 600;
            color: #333;
        }

        .cta-button {
            display: inline-block;
            background-color: #10b981;
            color: #ffffff !important;
            text-decoration: none;
            padding: 12px 30px;
            border-radius: 6px;
            font-weight: 500;
            margin: 20px 0;
            text-align: center;
        }

        .email-footer {
            background-color: #f8f9fa;
            padding: 25px 30px;
            text-align: center;
            color: #757575;
            font-size: 14px;
            border-top: 1px solid #e0e0e0;
        }

        .footer-links a {
            color: #1a73e8;
            text-decoration: none;
            margin: 0 10px;
        }

        @media only screen and (max-width: 600px) {
            .email-wrapper {
                border-radius: 0;
            }
            .email-body {
                padding: 30px 20px;
            }
            .info-row {
                flex-direction: column;
            }
        }
    </style>
</head>
<body>
    <div class="email-wrapper">
        <div class="email-header">
            <div class="logo">
                <img src="https://admin.mabsplace.com/_next/static/media/mabsplace_light.55f402f2.png" alt="MabsPlace">
            </div>
            <h1 class="header-title">Subscription Renewed! 🎉</h1>
        </div>

        <div class="email-body">
            <div class="greeting">Hello {{username}}!</div>
            <div class="content-block">
                <p>Great news! Your subscription has been successfully renewed.</p>

                <div class="info-box">
                    <div class="info-row">
                        <span class="info-label">Service:</span>
                        <span class="info-value">{{service_name}}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Profile:</span>
                        <span class="info-value">{{profile_name}}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Valid Until:</span>
                        <span class="info-value">{{new_end_date}}</span>
                    </div>
                </div>

                <p>You can continue enjoying uninterrupted access to your service.</p>

                <center>
                    <a href="https://mabsplace.app/subscriptions" class="cta-button">View My Subscriptions</a>
                </center>

                <p style="font-size: 14px; color: #757575; margin-top: 20px;">
                    Need help? Contact our support team anytime at <a href="mailto:support@mabsplace.com" style="color: #1a73e8;">support@mabsplace.com</a>
                </p>
            </div>
        </div>

        <div class="email-footer">
            <div class="footer-links">
                <a href="https://mabsplace.app/subscriptions">My Subscriptions</a>
                <a href="https://mabsplace.app/support">Support</a>
            </div>
            <div style="margin-top: 15px;">
                © 2025 MabsPlace. All rights reserved.
            </div>
        </div>
    </div>
</body>
</html>
```

---

## Template 3: Subscription Expiring

**Template Name:** `subscription-expiring`
**Subject Line:** `⏰ Your {{service_name}} Subscription Expires in {{days_remaining}} Days`
**Variables:** `username`, `service_name`, `expiry_date`, `days_remaining`

**HTML Body:**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Subscription Expiring Soon</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Poppins', Arial, sans-serif;
            background-color: #f4f7fa;
            color: #333;
            line-height: 1.6;
        }

        .email-wrapper {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        }

        .email-header {
            background: linear-gradient(135deg, #f59e0b, #d97706);
            padding: 30px 20px;
            text-align: center;
        }

        .logo img {
            height: 40px;
            margin-bottom: 15px;
        }

        .header-title {
            color: #ffffff;
            font-size: 24px;
            font-weight: 600;
            margin: 0;
        }

        .email-body {
            padding: 40px 30px;
            color: #4a4a4a;
        }

        .greeting {
            font-size: 18px;
            font-weight: 500;
            margin-bottom: 20px;
        }

        .content-block {
            margin-bottom: 25px;
        }

        .content-block p {
            margin-bottom: 15px;
            font-size: 16px;
        }

        .warning-box {
            background-color: #fffbeb;
            border-left: 4px solid #f59e0b;
            padding: 20px;
            margin: 25px 0;
            border-radius: 4px;
        }

        .countdown {
            text-align: center;
            background: linear-gradient(135deg, #f59e0b, #d97706);
            color: #ffffff;
            padding: 20px;
            border-radius: 8px;
            margin: 25px 0;
        }

        .countdown-number {
            font-size: 48px;
            font-weight: 700;
            display: block;
        }

        .countdown-label {
            font-size: 14px;
            text-transform: uppercase;
            letter-spacing: 2px;
        }

        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid #e0e0e0;
        }

        .info-label {
            font-weight: 500;
            color: #666;
        }

        .info-value {
            font-weight: 600;
            color: #333;
        }

        .cta-button {
            display: inline-block;
            background-color: #f59e0b;
            color: #ffffff !important;
            text-decoration: none;
            padding: 12px 30px;
            border-radius: 6px;
            font-weight: 500;
            margin: 20px 0;
            text-align: center;
        }

        .email-footer {
            background-color: #f8f9fa;
            padding: 25px 30px;
            text-align: center;
            color: #757575;
            font-size: 14px;
            border-top: 1px solid #e0e0e0;
        }

        .footer-links a {
            color: #1a73e8;
            text-decoration: none;
            margin: 0 10px;
        }

        @media only screen and (max-width: 600px) {
            .email-wrapper {
                border-radius: 0;
            }
            .email-body {
                padding: 30px 20px;
            }
            .countdown-number {
                font-size: 36px;
            }
        }
    </style>
</head>
<body>
    <div class="email-wrapper">
        <div class="email-header">
            <div class="logo">
                <img src="https://admin.mabsplace.com/_next/static/media/mabsplace_light.55f402f2.png" alt="MabsPlace">
            </div>
            <h1 class="header-title">Subscription Expiring Soon ⏰</h1>
        </div>

        <div class="email-body">
            <div class="greeting">Hello {{username}}!</div>
            <div class="content-block">
                <p>This is a friendly reminder that your subscription is expiring soon.</p>

                <div class="countdown">
                    <span class="countdown-number">{{days_remaining}}</span>
                    <span class="countdown-label">Days Remaining</span>
                </div>

                <div class="warning-box">
                    <div class="info-row">
                        <span class="info-label">Service:</span>
                        <span class="info-value">{{service_name}}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Expiration Date:</span>
                        <span class="info-value">{{expiry_date}}</span>
                    </div>
                </div>

                <p><strong>What happens next?</strong></p>
                <ul style="margin-left: 20px; margin-bottom: 20px;">
                    <li>Your subscription will automatically renew if auto-renewal is enabled</li>
                    <li>Make sure you have sufficient balance in your wallet</li>
                    <li>Contact us if you need any assistance</li>
                </ul>

                <center>
                    <a href="https://mabsplace.app/wallet" class="cta-button">Check My Wallet Balance</a>
                </center>

                <p style="font-size: 14px; color: #757575; margin-top: 20px;">
                    Need to make changes to your subscription? Visit your <a href="https://mabsplace.app/subscriptions" style="color: #1a73e8;">subscriptions page</a>.
                </p>
            </div>
        </div>

        <div class="email-footer">
            <div class="footer-links">
                <a href="https://mabsplace.app/subscriptions">My Subscriptions</a>
                <a href="https://mabsplace.app/wallet">My Wallet</a>
                <a href="https://mabsplace.app/support">Support</a>
            </div>
            <div style="margin-top: 15px;">
                © 2025 MabsPlace. All rights reserved.
            </div>
        </div>
    </div>
</body>
</html>
```

---

## Template 4: Subscription Expired

**Template Name:** `subscription-expired`
**Subject Line:** `Your {{service_name}} Subscription Has Expired`
**Variables:** `username`, `service_name`, `profile_name`

**HTML Body:**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Subscription Expired</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Poppins', Arial, sans-serif;
            background-color: #f4f7fa;
            color: #333;
            line-height: 1.6;
        }

        .email-wrapper {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        }

        .email-header {
            background: linear-gradient(135deg, #ef4444, #dc2626);
            padding: 30px 20px;
            text-align: center;
        }

        .logo img {
            height: 40px;
            margin-bottom: 15px;
        }

        .header-title {
            color: #ffffff;
            font-size: 24px;
            font-weight: 600;
            margin: 0;
        }

        .email-body {
            padding: 40px 30px;
            color: #4a4a4a;
        }

        .greeting {
            font-size: 18px;
            font-weight: 500;
            margin-bottom: 20px;
        }

        .content-block {
            margin-bottom: 25px;
        }

        .content-block p {
            margin-bottom: 15px;
            font-size: 16px;
        }

        .error-box {
            background-color: #fef2f2;
            border-left: 4px solid #ef4444;
            padding: 20px;
            margin: 25px 0;
            border-radius: 4px;
        }

        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid #fecaca;
        }

        .info-row:last-child {
            border-bottom: none;
        }

        .info-label {
            font-weight: 500;
            color: #666;
        }

        .info-value {
            font-weight: 600;
            color: #333;
        }

        .cta-button {
            display: inline-block;
            background-color: #1a73e8;
            color: #ffffff !important;
            text-decoration: none;
            padding: 12px 30px;
            border-radius: 6px;
            font-weight: 500;
            margin: 20px 0;
            text-align: center;
        }

        .steps-list {
            background-color: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin: 20px 0;
        }

        .steps-list li {
            margin-bottom: 10px;
            padding-left: 10px;
        }

        .email-footer {
            background-color: #f8f9fa;
            padding: 25px 30px;
            text-align: center;
            color: #757575;
            font-size: 14px;
            border-top: 1px solid #e0e0e0;
        }

        .footer-links a {
            color: #1a73e8;
            text-decoration: none;
            margin: 0 10px;
        }

        @media only screen and (max-width: 600px) {
            .email-wrapper {
                border-radius: 0;
            }
            .email-body {
                padding: 30px 20px;
            }
            .info-row {
                flex-direction: column;
            }
        }
    </style>
</head>
<body>
    <div class="email-wrapper">
        <div class="email-header">
            <div class="logo">
                <img src="https://admin.mabsplace.com/_next/static/media/mabsplace_light.55f402f2.png" alt="MabsPlace">
            </div>
            <h1 class="header-title">Subscription Expired</h1>
        </div>

        <div class="email-body">
            <div class="greeting">Hello {{username}},</div>
            <div class="content-block">
                <p>Your subscription has expired and access to your service has been disabled.</p>

                <div class="error-box">
                    <div class="info-row">
                        <span class="info-label">Service:</span>
                        <span class="info-value">{{service_name}}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Profile:</span>
                        <span class="info-value">{{profile_name}}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Status:</span>
                        <span class="info-value" style="color: #ef4444;">EXPIRED</span>
                    </div>
                </div>

                <p><strong>Want to renew?</strong></p>
                <div class="steps-list">
                    <ol style="margin-left: 20px;">
                        <li>Top up your wallet with sufficient funds</li>
                        <li>Go to your subscriptions page</li>
                        <li>Click "Renew" on your expired subscription</li>
                        <li>Enjoy uninterrupted access again!</li>
                    </ol>
                </div>

                <center>
                    <a href="https://mabsplace.app/subscriptions" class="cta-button">Renew My Subscription</a>
                </center>

                <p style="font-size: 14px; color: #757575; margin-top: 20px;">
                    Need help renewing? Our support team is here to assist you at <a href="mailto:support@mabsplace.com" style="color: #1a73e8;">support@mabsplace.com</a>
                </p>
            </div>
        </div>

        <div class="email-footer">
            <div class="footer-links">
                <a href="https://mabsplace.app/subscriptions">My Subscriptions</a>
                <a href="https://mabsplace.app/wallet">Add Funds</a>
                <a href="https://mabsplace.app/support">Support</a>
            </div>
            <div style="margin-top: 15px;">
                © 2025 MabsPlace. All rights reserved.
            </div>
        </div>
    </div>
</body>
</html>
```

---

## Template 5: Renewal Failed

**Template Name:** `renewal-failed`
**Subject Line:** `⚠️ Failed to Renew Your {{service_name}} Subscription (Attempt {{attempt_number}})`
**Variables:** `username`, `service_name`, `attempt_number`

**HTML Body:**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Renewal Failed</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Poppins', Arial, sans-serif;
            background-color: #f4f7fa;
            color: #333;
            line-height: 1.6;
        }

        .email-wrapper {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        }

        .email-header {
            background: linear-gradient(135deg, #f97316, #ea580c);
            padding: 30px 20px;
            text-align: center;
        }

        .logo img {
            height: 40px;
            margin-bottom: 15px;
        }

        .header-title {
            color: #ffffff;
            font-size: 24px;
            font-weight: 600;
            margin: 0;
        }

        .email-body {
            padding: 40px 30px;
            color: #4a4a4a;
        }

        .greeting {
            font-size: 18px;
            font-weight: 500;
            margin-bottom: 20px;
        }

        .content-block {
            margin-bottom: 25px;
        }

        .content-block p {
            margin-bottom: 15px;
            font-size: 16px;
        }

        .warning-box {
            background-color: #fff7ed;
            border-left: 4px solid #f97316;
            padding: 20px;
            margin: 25px 0;
            border-radius: 4px;
        }

        .attempt-badge {
            display: inline-block;
            background-color: #f97316;
            color: #ffffff;
            padding: 8px 16px;
            border-radius: 20px;
            font-weight: 600;
            font-size: 14px;
            margin: 10px 0;
        }

        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid #fed7aa;
        }

        .info-row:last-child {
            border-bottom: none;
        }

        .info-label {
            font-weight: 500;
            color: #666;
        }

        .info-value {
            font-weight: 600;
            color: #333;
        }

        .cta-button {
            display: inline-block;
            background-color: #1a73e8;
            color: #ffffff !important;
            text-decoration: none;
            padding: 12px 30px;
            border-radius: 6px;
            font-weight: 500;
            margin: 20px 0;
            text-align: center;
        }

        .reasons-list {
            background-color: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin: 20px 0;
        }

        .reasons-list li {
            margin-bottom: 10px;
            padding-left: 10px;
        }

        .email-footer {
            background-color: #f8f9fa;
            padding: 25px 30px;
            text-align: center;
            color: #757575;
            font-size: 14px;
            border-top: 1px solid #e0e0e0;
        }

        .footer-links a {
            color: #1a73e8;
            text-decoration: none;
            margin: 0 10px;
        }

        @media only screen and (max-width: 600px) {
            .email-wrapper {
                border-radius: 0;
            }
            .email-body {
                padding: 30px 20px;
            }
            .info-row {
                flex-direction: column;
            }
        }
    </style>
</head>
<body>
    <div class="email-wrapper">
        <div class="email-header">
            <div class="logo">
                <img src="https://admin.mabsplace.com/_next/static/media/mabsplace_light.55f402f2.png" alt="MabsPlace">
            </div>
            <h1 class="header-title">Renewal Failed ⚠️</h1>
        </div>

        <div class="email-body">
            <div class="greeting">Hello {{username}},</div>
            <div class="content-block">
                <p>We were unable to automatically renew your subscription.</p>

                <div style="text-align: center;">
                    <span class="attempt-badge">Attempt {{attempt_number}} of 4</span>
                </div>

                <div class="warning-box">
                    <div class="info-row">
                        <span class="info-label">Service:</span>
                        <span class="info-value">{{service_name}}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Renewal Status:</span>
                        <span class="info-value" style="color: #f97316;">FAILED</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Next Attempt:</span>
                        <span class="info-value">Within 24 hours</span>
                    </div>
                </div>

                <p><strong>Common reasons for renewal failure:</strong></p>
                <div class="reasons-list">
                    <ul style="margin-left: 20px;">
                        <li>Insufficient wallet balance</li>
                        <li>Payment method expired or invalid</li>
                        <li>Temporary system issue</li>
                    </ul>
                </div>

                <p><strong>What you should do:</strong></p>
                <p>1. Check your wallet balance and add funds if needed<br>
                2. We will automatically retry the renewal within 24 hours<br>
                3. After 4 failed attempts, your subscription will be cancelled</p>

                <center>
                    <a href="https://mabsplace.app/wallet" class="cta-button">Add Funds to Wallet</a>
                </center>

                <p style="font-size: 14px; color: #757575; margin-top: 20px;">
                    Need immediate assistance? Contact our support team at <a href="mailto:support@mabsplace.com" style="color: #1a73e8;">support@mabsplace.com</a>
                </p>
            </div>
        </div>

        <div class="email-footer">
            <div class="footer-links">
                <a href="https://mabsplace.app/subscriptions">My Subscriptions</a>
                <a href="https://mabsplace.app/wallet">My Wallet</a>
                <a href="https://mabsplace.app/support">Support</a>
            </div>
            <div style="margin-top: 15px;">
                © 2025 MabsPlace. All rights reserved.
            </div>
        </div>
    </div>
</body>
</html>
```

---

## Template 6: Payment Reminder (Admin)

**Template Name:** `payment-reminder`
**Subject Line:** `💳 Payment Due Soon: {{service_name}} - {{days_remaining}} Days`
**Variables:** `service_name`, `account_login`, `payment_date`, `days_remaining`

**Note:** This template is for admin notifications only.

**HTML Body:**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment Reminder</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Poppins', Arial, sans-serif;
            background-color: #f4f7fa;
            color: #333;
            line-height: 1.6;
        }

        .email-wrapper {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        }

        .email-header {
            background: linear-gradient(135deg, #6366f1, #4f46e5);
            padding: 30px 20px;
            text-align: center;
        }

        .logo img {
            height: 40px;
            margin-bottom: 15px;
        }

        .header-title {
            color: #ffffff;
            font-size: 24px;
            font-weight: 600;
            margin: 0;
        }

        .email-body {
            padding: 40px 30px;
            color: #4a4a4a;
        }

        .greeting {
            font-size: 18px;
            font-weight: 500;
            margin-bottom: 20px;
        }

        .content-block {
            margin-bottom: 25px;
        }

        .info-box {
            background-color: #eef2ff;
            border-left: 4px solid #6366f1;
            padding: 20px;
            margin: 25px 0;
            border-radius: 4px;
        }

        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid #c7d2fe;
        }

        .info-row:last-child {
            border-bottom: none;
        }

        .info-label {
            font-weight: 500;
            color: #666;
        }

        .info-value {
            font-weight: 600;
            color: #333;
        }

        .email-footer {
            background-color: #f8f9fa;
            padding: 25px 30px;
            text-align: center;
            color: #757575;
            font-size: 14px;
            border-top: 1px solid #e0e0e0;
        }

        @media only screen and (max-width: 600px) {
            .email-wrapper {
                border-radius: 0;
            }
            .email-body {
                padding: 30px 20px;
            }
        }
    </style>
</head>
<body>
    <div class="email-wrapper">
        <div class="email-header">
            <div class="logo">
                <img src="https://admin.mabsplace.com/_next/static/media/mabsplace_light.55f402f2.png" alt="MabsPlace">
            </div>
            <h1 class="header-title">Payment Reminder 💳</h1>
        </div>

        <div class="email-body">
            <div class="greeting">Hello Admin,</div>
            <div class="content-block">
                <p>This is a reminder about an upcoming payment for a service account.</p>

                <div class="info-box">
                    <div class="info-row">
                        <span class="info-label">Service:</span>
                        <span class="info-value">{{service_name}}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Account Login:</span>
                        <span class="info-value">{{account_login}}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Payment Date:</span>
                        <span class="info-value">{{payment_date}}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Days Remaining:</span>
                        <span class="info-value">{{days_remaining}}</span>
                    </div>
                </div>

                <p>Please ensure payment is processed on time to avoid service interruption.</p>
            </div>
        </div>

        <div class="email-footer">
            <div style="margin-top: 15px;">
                © 2025 MabsPlace Admin. All rights reserved.
            </div>
        </div>
    </div>
</body>
</html>
```

---

## Quick Setup Checklist

- [ ] Template 1: verification-code
- [ ] Template 2: subscription-renewed
- [ ] Template 3: subscription-expiring
- [ ] Template 4: subscription-expired
- [ ] Template 5: renewal-failed
- [ ] Template 6: payment-reminder

## Testing

After creating all templates in OneSignal:

1. Test email verification by creating a new account
2. Test subscription notifications by triggering subscription events
3. Verify all variables are replaced correctly
4. Check email rendering across different email clients (Gmail, Outlook, etc.)

## Support

For issues with OneSignal setup:
- OneSignal Docs: https://documentation.onesignal.com/
- OneSignal Support: support@onesignal.com
