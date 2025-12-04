#!/bin/bash

# Railway Setup Script for Dashboard Performance Updates
# This script helps you complete the Railway deployment

echo "🚂 Railway Dashboard Performance Setup"
echo "======================================"
echo ""

# Check if railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "❌ Railway CLI not found. Install it first:"
    echo "   npm install -g @railway/cli"
    exit 1
fi

echo "✅ Railway CLI found"
echo ""

# Check if project is linked
if ! railway status &> /dev/null; then
    echo "❌ Not linked to Railway project"
    echo "   Run: railway link -p 1c1a62c2-e51d-4ce4-a182-1f8133156b7b"
    exit 1
fi

echo "✅ Project linked: $(railway status | grep 'Project:' | cut -d':' -f2)"
echo ""

echo "📋 Manual Steps Required:"
echo ""
echo "STEP 1: Add Redis Service"
echo "-------------------------"
echo "1. Go to: https://railway.app/project/1c1a62c2-e51d-4ce4-a182-1f8133156b7b"
echo "2. Click '+ New' button"
echo "3. Select 'Database' → 'Add Redis'"
echo "4. Wait for Redis to deploy (~1-2 minutes)"
echo ""

read -p "Press ENTER when Redis is added and ready..."

echo ""
echo "STEP 2: Verify Redis Environment Variables"
echo "------------------------------------------"
echo "Checking for Redis environment variables..."
echo ""

if railway variables | grep -q "REDIS"; then
    echo "✅ Redis variables found:"
    railway variables | grep REDIS
else
    echo "⚠️  Redis variables not detected yet"
    echo "   The Redis service may still be initializing"
    echo "   Check Railway dashboard and wait a moment"
fi

echo ""
echo "STEP 3: Apply Database Indexes"
echo "-------------------------------"
echo "Connect to MySQL and run the indexes:"
echo ""
echo "Option A: Via Railway Dashboard"
echo "1. Go to your MySQL service"
echo "2. Click 'Data' tab → 'Connect'"
echo "3. Copy the performance_indexes.sql content"
echo "4. Paste and execute in the MySQL console"
echo ""
echo "Option B: Via Command Line"
echo "  railway run mysql -h mysql -u root -p mabsplace < src/main/resources/db/performance_indexes.sql"
echo ""

read -p "Press ENTER when database indexes are applied..."

echo ""
echo "STEP 4: Trigger Deployment"
echo "-------------------------"
echo "Railway will automatically deploy the new code."
echo "Or manually trigger: railway up"
echo ""

read -p "Trigger deployment now? (y/n): " deploy
if [[ $deploy == "y" ]]; then
    echo "Deploying..."
    railway up
fi

echo ""
echo "STEP 5: Verify Deployment"
echo "------------------------"
echo "Check logs for Redis connection:"
echo ""
echo "  railway logs"
echo ""
echo "Look for:"
echo "  ✓ 'Connected to Redis'"
echo "  ✓ No Redis connection errors"
echo ""

read -p "View logs now? (y/n): " logs
if [[ $logs == "y" ]]; then
    railway logs
fi

echo ""
echo "✅ Setup Complete!"
echo ""
echo "Next Steps:"
echo "1. Test dashboard: https://mabsplace.com/dashboard"
echo "2. Check Redis cache: railway connect redis → KEYS *"
echo "3. Monitor performance in Railway dashboard"
echo ""
echo "Expected Results:"
echo "- First load: Normal speed (populates cache)"
echo "- Subsequent loads: 5-10x faster! 🚀"
echo ""
