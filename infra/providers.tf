# Primary provider: bucket and its region-specific sub-resources live in eu-north-1.
provider "aws" {
  region = "eu-north-1"
}

# ACM certificates used by CloudFront must exist in us-east-1 regardless of
# where the rest of the stack lives. We only *read* the existing wildcard
# cert here (it's shared with other brotherus.net subdomains/apps and is
# managed outside this project).
provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"
}
