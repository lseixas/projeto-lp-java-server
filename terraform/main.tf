terraform {
  required_version = ">= 1.0"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  backend "s3" {
    # Uncomment and configure when ready to use S3 backend
    # bucket = "your-terraform-state-bucket"
    # key    = "projeto-lp-java-server/terraform.tfstate"
    # region = "us-east-1"
  }
}

provider "aws" {
  region = var.aws_region
  
  default_tags {
    tags = {
      Project     = "projeto-lp-java-server"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}
