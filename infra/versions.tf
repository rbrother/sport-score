terraform {
  required_version = ">= 1.6"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # State is kept locally (.gitignored) since this is a single-maintainer
  # hobby project. If collaborators are added later, switch this to an
  # S3 backend with DynamoDB locking.
}
