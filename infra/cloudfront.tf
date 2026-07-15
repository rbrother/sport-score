# Existing wildcard cert (*.brotherus.net) shared across several apps/domains.
# Managed outside this project — looked up read-only, never created/destroyed here.
data "aws_acm_certificate" "wildcard" {
  provider    = aws.us_east_1
  domain      = var.acm_certificate_domain
  statuses    = ["ISSUED"]
  most_recent = true
}

# AWS managed cache policy: "CachingOptimized".
data "aws_cloudfront_cache_policy" "caching_optimized" {
  name = "Managed-CachingOptimized"
}

resource "aws_cloudfront_distribution" "site" {
  comment             = "Sport Tracker SPA"
  enabled             = true
  is_ipv6_enabled     = true
  http_version        = "http2and3"
  price_class         = "PriceClass_100"
  aliases             = [var.domain_name]
  default_root_object = ""

  tags = {
    Name = "Sport Tracker"
  }

  origin {
    domain_name = aws_s3_bucket_website_configuration.site.website_endpoint
    origin_id   = "sport-tracker.s3-website.eu-north-1.amazonaws.com-mfcrlh0odei"

    custom_origin_config {
      http_port              = 80
      https_port              = 443
      origin_protocol_policy  = "http-only"
      origin_ssl_protocols    = ["SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"]
      origin_read_timeout     = 30
      origin_keepalive_timeout = 5
    }
  }

  default_cache_behavior {
    target_origin_id       = "sport-tracker.s3-website.eu-north-1.amazonaws.com-mfcrlh0odei"
    viewer_protocol_policy = "redirect-to-https"
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]
    compress               = true
    cache_policy_id        = data.aws_cloudfront_cache_policy.caching_optimized.id
  }

  # SPA client-side routing: any 403/404 from the S3 website origin (unknown
  # path) is rewritten to index.html with a 200 so the app's router can
  # handle it, instead of showing a raw S3 error page.
  custom_error_response {
    error_code         = 403
    response_code      = 200
    response_page_path = "/index.html"
  }

  custom_error_response {
    error_code         = 404
    response_code      = 200
    response_page_path = "/index.html"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = data.aws_acm_certificate.wildcard.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }
}
