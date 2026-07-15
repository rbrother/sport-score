# Hosted zone is shared with other subdomains/apps and managed outside this
# project — looked up read-only rather than imported/managed here.
data "aws_route53_zone" "brotherus" {
  name = var.hosted_zone_name
}

resource "aws_route53_record" "squash_a" {
  zone_id = data.aws_route53_zone.brotherus.zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.site.domain_name
    zone_id                = aws_cloudfront_distribution.site.hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "squash_aaaa" {
  zone_id = data.aws_route53_zone.brotherus.zone_id
  name    = var.domain_name
  type    = "AAAA"

  alias {
    name                   = aws_cloudfront_distribution.site.domain_name
    zone_id                = aws_cloudfront_distribution.site.hosted_zone_id
    evaluate_target_health = false
  }
}
