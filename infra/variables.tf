variable "bucket_name" {
  description = "S3 bucket that hosts the compiled SPA assets"
  type        = string
  default     = "sport-tracker"
}

variable "domain_name" {
  description = "Public hostname the app is served on"
  type        = string
  default     = "squash.brotherus.net"
}

variable "hosted_zone_name" {
  description = "Route53 hosted zone that owns domain_name (shared across multiple subdomains/apps)"
  type        = string
  default     = "brotherus.net."
}

variable "acm_certificate_domain" {
  description = "Domain of the existing wildcard ACM certificate to reuse for CloudFront (must live in us-east-1)"
  type        = string
  default     = "*.brotherus.net"
}
