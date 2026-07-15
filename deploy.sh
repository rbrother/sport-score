#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Infra (S3 bucket, CloudFront distribution, DNS) is managed via Terraform in
# ./infra - see infra/README.md. Read the bucket/distribution id from its
# state so this script and Terraform never drift apart, falling back to the
# known values if infra/ hasn't been initialized locally.
if terraform -chdir="$SCRIPT_DIR/infra" output -raw cloudfront_distribution_id >/dev/null 2>&1; then
  CLOUDFRONT_DISTRIBUTION_ID=$(terraform -chdir="$SCRIPT_DIR/infra" output -raw cloudfront_distribution_id)
  BUCKET_NAME=$(terraform -chdir="$SCRIPT_DIR/infra" output -raw s3_bucket_name)
else
  CLOUDFRONT_DISTRIBUTION_ID=E2XY0E8VX1E52P
  BUCKET_NAME=sport-tracker
fi

npm install
rm -rf ./resources/public/js/compiled
npm run release

# no-cache forces browsers/CDN to revalidate (via ETag) on every load instead of
# using stale cached copies - fixes mobile browsers showing old versions after deploy
aws s3 cp ./resources/public "s3://$BUCKET_NAME/" --recursive \
  --cache-control "no-cache, must-revalidate"

# Invalidate CloudFront edge caches so the new files are served immediately
aws cloudfront create-invalidation \
  --distribution-id "$CLOUDFRONT_DISTRIBUTION_ID" \
  --paths "/*"
