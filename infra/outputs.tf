output "cloudfront_distribution_id" {
  description = "Used by deploy.sh for cache invalidation"
  value       = aws_cloudfront_distribution.site.id
}

output "s3_bucket_name" {
  description = "Used by deploy.sh as the upload target"
  value       = aws_s3_bucket.site.id
}

output "cloudfront_domain_name" {
  value = aws_cloudfront_distribution.site.domain_name
}

output "site_url" {
  value = "https://${var.domain_name}/"
}
