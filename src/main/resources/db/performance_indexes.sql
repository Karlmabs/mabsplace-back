-- Performance indexes for dashboard queries
-- Run this SQL script to improve dashboard query performance

-- Indexes for payments table (frequently queried for revenue calculations)
CREATE INDEX IF NOT EXISTS idx_payments_date_status ON payments(payment_date, status);
CREATE INDEX IF NOT EXISTS idx_payments_user_date ON payments(user_id, payment_date);
CREATE INDEX IF NOT EXISTS idx_payments_service_date ON payments(service_id, payment_date, status);
CREATE INDEX IF NOT EXISTS idx_payments_plan_date ON payments(subscription_plan_id, payment_date, status);

-- Indexes for subscriptions table (core to most dashboard queries)
CREATE INDEX IF NOT EXISTS idx_subscriptions_dates_status ON subscriptions(start_date, end_date, status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_status ON subscriptions(user_id, status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_service_status ON subscriptions(service_id, status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_profile_status ON subscriptions(profile_id, status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_trial ON subscriptions(is_trial, status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_auto_renew ON subscriptions(auto_renew, status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_renewal_attempts ON subscriptions(renewal_attempts, status);

-- Indexes for expenses table (for expense trending and categorization)
CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses(expense_date);
CREATE INDEX IF NOT EXISTS idx_expenses_category_date ON expenses(category_id, expense_date);
CREATE INDEX IF NOT EXISTS idx_expenses_recurring_date ON expenses(is_recurring, expense_date);

-- Indexes for profiles table (for utilization tracking)
CREATE INDEX IF NOT EXISTS idx_profiles_status ON profiles(status);
CREATE INDEX IF NOT EXISTS idx_profiles_account_status ON profiles(service_account_id, status);

-- Indexes for service accounts
CREATE INDEX IF NOT EXISTS idx_service_accounts_service ON service_accounts(my_service_id);

-- Indexes for subscription plans (frequently joined)
CREATE INDEX IF NOT EXISTS idx_subscription_plans_service ON subscription_plans(service_id);
CREATE INDEX IF NOT EXISTS idx_subscription_plans_active ON subscription_plans(is_active);

-- Composite index for common subscription queries
CREATE INDEX IF NOT EXISTS idx_subscriptions_active_dates ON subscriptions(status, start_date, end_date)
WHERE status = 'ACTIVE';

-- Index for trial conversions
CREATE INDEX IF NOT EXISTS idx_subscriptions_trial_user ON subscriptions(user_id, is_trial);

-- Index for payment analysis
CREATE INDEX IF NOT EXISTS idx_payments_status_date_user ON payments(status, payment_date, user_id)
WHERE status = 'PAID';

-- Display index creation results
SELECT 'Dashboard performance indexes created successfully' AS result;
