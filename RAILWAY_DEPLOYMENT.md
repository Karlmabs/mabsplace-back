# Railway Deployment Guide - Dashboard Performance Updates

This guide covers deploying the dashboard performance improvements to Railway.

## 🚂 Prerequisites

You need to add Redis to your Railway project for caching to work.

## Step 1: Add Redis Service to Railway

### Option A: Via Railway Dashboard (Recommended)
1. Go to your Railway project: https://railway.app/project/[your-project-id]
2. Click **"+ New"** button
3. Select **"Database"** → **"Add Redis"**
4. Railway will automatically:
   - Create a Redis instance
   - Generate connection credentials
   - Set environment variables: `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`

### Option B: Via Railway CLI
```bash
# Login to Railway
railway login

# Link to your project
railway link

# Add Redis service
railway add

# Select Redis from the list
```

## Step 2: Verify Environment Variables

After adding Redis, verify these environment variables are set in your Railway backend service:

```bash
railway variables
```

You should see:
- `REDIS_HOST` - Redis host URL
- `REDIS_PORT` - Redis port (usually 6379)
- `REDIS_PASSWORD` - Redis password (if set)

**Note:** The application is configured to use these environment variables automatically via `application-prod.yml`:
```yaml
redis:
  host: ${REDIS_HOST:localhost}
  port: ${REDIS_PORT:6379}
  password: ${REDIS_PASSWORD:}
```

## Step 3: Deploy Database Indexes

The performance indexes need to be applied to your production MySQL database.

### Connect to Railway MySQL Database

```bash
# Option 1: Via Railway Dashboard
1. Go to your MySQL service in Railway
2. Click "Data" tab
3. Click "Connect" → "MySQL CLI"
4. Copy the connection command

# Option 2: Via Railway CLI
railway run mysql -h mysql -u root -p mabsplace
```

### Run the Index Script

Once connected to MySQL:

```sql
-- Copy and paste the entire contents of:
-- src/main/resources/db/performance_indexes.sql

-- Or run it directly if you have the file:
SOURCE /path/to/performance_indexes.sql;
```

**Important Indexes Being Created:**
- Payments table: 4 indexes (date, user, service, plan filters)
- Subscriptions table: 11 indexes (dates, status, trials, renewals)
- Expenses table: 3 indexes (date, category, recurring)
- Profiles table: 2 indexes (status, service account)
- Additional composite indexes for common queries

## Step 4: Verify Indexes Were Created

```sql
-- Check indexes on payments table
SHOW INDEX FROM payments;

-- Check indexes on subscriptions table
SHOW INDEX FROM subscriptions;

-- Check indexes on expenses table
SHOW INDEX FROM expenses;

-- Check indexes on profiles table
SHOW INDEX FROM profiles;
```

## Step 5: Deploy Backend Changes

The code is already pushed to GitHub. Railway will automatically redeploy when it detects the changes:

1. **Automatic Deployment:**
   - Railway watches your GitHub repository
   - It will automatically detect the new commits
   - It will build and deploy the new version

2. **Manual Deployment (if needed):**
   ```bash
   railway up
   ```

## Step 6: Verify Deployment

### Check Redis Connection

After deployment, verify Redis is working:

```bash
# View logs
railway logs

# Look for messages like:
# "Lettuce: Trying to get connection to Redis"
# "Connected to Redis successfully"
```

### Test Dashboard Performance

1. Visit your dashboard: `https://mabsplace.app/dashboard`
2. First load: Should execute all queries (check logs)
3. Refresh within 5 minutes: Should use cached data (much faster!)
4. Check Redis cache:
   ```bash
   # Connect to Redis
   railway connect redis

   # In Redis CLI:
   KEYS *
   # Should show cache keys like: dashboardStats::all, revenueTrend::all

   TTL dashboardStats::all
   # Should show remaining seconds until cache expires
   ```

## Step 7: Monitor Performance

### Check Cache Hit Rate

Railway Redis provides metrics. Check:
- Cache hits vs misses
- Memory usage
- Active connections

### Database Query Performance

After applying indexes, your slow query log should show improved times for:
- Dashboard stats queries
- Revenue trend calculations
- Subscription health metrics

## Configuration Summary

| Config | Development | Production (Railway) |
|--------|------------|---------------------|
| Redis Host | localhost | ${REDIS_HOST} from Railway |
| Redis Port | 6379 | ${REDIS_PORT} from Railway |
| Redis Password | (none) | ${REDIS_PASSWORD} from Railway |
| Cache TTL - Stats | 5 minutes | 5 minutes |
| Cache TTL - Trends | 15 minutes | 15 minutes |
| Cache TTL - Historical | 1 hour | 1 hour |

## Troubleshooting

### Redis Connection Errors

**Error:** `Unable to connect to Redis`

**Solution:**
1. Verify Redis service is running in Railway
2. Check environment variables are set correctly
3. Ensure backend service can reach Redis (same Railway project)

### No Performance Improvement

**Check:**
1. Are indexes created? Run `SHOW INDEX FROM payments;`
2. Is Redis connected? Check logs for Redis connection messages
3. Is caching working? Check Redis keys with `KEYS *`
4. Clear cache if stale: In Redis CLI run `FLUSHDB`

### Cache Not Expiring

Railway Redis is persistent. To clear all cache:

```bash
railway connect redis
# In Redis CLI:
FLUSHDB
```

## Expected Performance Improvements

### Before Optimization
- Dashboard load time: ~2-5 seconds (12+ database queries)
- Revenue trend query: ~500-1000ms
- Subscription health: ~800ms

### After Optimization
- **First load:** Similar (populates cache)
- **Cached loads:** ~100-300ms (80-90% faster!)
- **Database queries:** 50-70% faster with indexes
- **Overall:** Dashboard 5-10x faster on subsequent loads

## Cost Considerations

**Railway Redis Pricing:**
- Hobby Plan: Includes some Redis usage
- Pro Plan: More generous limits
- Check your Railway billing for Redis costs

**Tip:** The cache TTLs are configured to balance:
- Performance (longer cache = faster loads)
- Data freshness (shorter cache = more up-to-date)
- Costs (longer cache = fewer DB queries)

## Rollback Plan

If you encounter issues:

### Disable Caching Temporarily

Set environment variable in Railway:
```bash
SPRING_CACHE_TYPE=none
```

This will disable caching while keeping all other features.

### Rollback Deployment

```bash
# Via Railway CLI
railway rollback

# Or via Dashboard
# Go to Deployments → Select previous version → Redeploy
```

## Support

If you encounter issues:
1. Check Railway logs: `railway logs`
2. Check Redis status in Railway dashboard
3. Verify MySQL indexes: `SHOW INDEX FROM [table_name];`
4. Test locally first with Redis running: `redis-server`

---

## Summary Checklist

- [ ] Redis service added to Railway project
- [ ] Environment variables verified (REDIS_HOST, REDIS_PORT, REDIS_PASSWORD)
- [ ] Database indexes applied via performance_indexes.sql
- [ ] Backend deployed with caching code
- [ ] Redis connection verified in logs
- [ ] Dashboard tested (first load vs cached load)
- [ ] Cache expiration verified (TTL working)
- [ ] Performance improvement confirmed

🎉 Once complete, your dashboard will be significantly faster!
