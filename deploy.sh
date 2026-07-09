#!/usr/bin/env bash
set -euo pipefail

CLOUDFRONT_DISTRIBUTION_ID=E2XY0E8VX1E52P

npm install
rm -rf ./resources/public/js/compiled
npm run release

# no-cache forces browsers/CDN to revalidate (via ETag) on every load instead of
# using stale cached copies - fixes mobile browsers showing old versions after deploy
aws s3 cp ./resources/public s3://sport-tracker/ --recursive \
  --cache-control "no-cache, must-revalidate"

# Invalidate CloudFront edge caches so the new files are served immediately
aws cloudfront create-invalidation \
  --distribution-id "$CLOUDFRONT_DISTRIBUTION_ID" \
  --paths "/*"
