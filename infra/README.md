# Infrastructure (Terraform)

This directory manages the AWS resources that host the deployed SPA:

- **S3 bucket** (`sport-tracker`) — static website hosting for the compiled
  app, with `index.html` as both the index and error document (the error
  document fallback is what lets client-side routes like `/year/2026` work
  on direct navigation/refresh).
- **CloudFront distribution** — CDN + TLS in front of the bucket's website
  endpoint, with 403/404 responses rewritten to `/index.html` (200) as a
  second layer of SPA-routing support.
- **Route53 records** — `A`/`AAAA` alias records pointing
  `squash.brotherus.net` at the CloudFront distribution.

Resources that are **shared with other, unrelated apps/domains** are looked
up read-only via Terraform data sources instead of being imported/managed
here, so a `terraform destroy` or bad plan in this project can never affect
them:
- The `brotherus.net` Route53 hosted zone (other subdomains live in it too).
- The `*.brotherus.net` wildcard ACM certificate (reused by other apps).

## Prerequisites

- Terraform >= 1.6 (a recent version is required — older CLI releases fail
  to verify the current HashiCorp provider signing key).
- AWS credentials with permissions for S3, CloudFront, ACM (read), and
  Route53, e.g. via `aws sso login` or environment variables.

## Usage

```bash
cd infra
terraform init
terraform plan    # review changes before applying
terraform apply
```

State is kept **locally** (`terraform.tfstate`, gitignored) since this is a
single-maintainer project. If more people start collaborating, migrate to a
remote backend (e.g. an S3 bucket + DynamoDB lock table) so state isn't only
on one machine.

`../deploy.sh` reads the bucket name and CloudFront distribution ID from
`terraform output` when available, so the app-deploy script and the infra
config can't drift apart.

## Making changes

All infra changes (new cache behaviors, error responses, DNS records, etc.)
should go through `terraform plan`/`apply` here rather than the AWS Console,
so they stay reviewable and reproducible. If someone does make a manual
console change, running `terraform plan` will show the drift.
