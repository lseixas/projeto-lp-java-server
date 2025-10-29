# Terraform Infrastructure for projeto-lp-java-server

This directory contains Terraform configurations for deploying the Java application to AWS.

## Prerequisites

- Terraform >= 1.0
- AWS CLI configured with appropriate credentials
- AWS account with necessary permissions

## Infrastructure Components

The Terraform configuration creates the following AWS resources:

- **VPC**: Virtual Private Cloud with public and private subnets across multiple availability zones
- **Internet Gateway**: For public internet access
- **NAT Gateway**: For private subnet internet access
- **Application Load Balancer**: For distributing traffic to application instances
- **Security Groups**: For network security
- **Target Groups**: For load balancer routing

## Configuration

### Variables

Key variables can be configured in `variables.tf` or via command-line:

- `aws_region`: AWS region (default: us-east-1)
- `environment`: Environment name (default: dev)
- `vpc_cidr`: VPC CIDR block (default: 10.0.0.0/16)
- `instance_type`: EC2 instance type (default: t3.micro)
- `app_port`: Application port (default: 8080)

### Backend Configuration

The S3 backend is commented out in `main.tf`. To use it:

1. Create an S3 bucket for Terraform state
2. Uncomment and configure the backend block in `main.tf`
3. Run `terraform init` to migrate state

## Usage

### Initialize Terraform

```bash
cd terraform
terraform init
```

### Plan Infrastructure Changes

```bash
terraform plan
```

### Apply Infrastructure

```bash
terraform apply
```

### Destroy Infrastructure

```bash
terraform destroy
```

## Deployment Options

This configuration provides the foundation for deploying the Java application. You can extend it with:

1. **EC2 Auto Scaling**: Add Auto Scaling Groups for EC2 instances
2. **ECS/Fargate**: Deploy containerized application
3. **RDS**: Add database instance
4. **ElastiCache**: Add caching layer
5. **CloudWatch**: Add monitoring and logging
6. **Route53**: Add DNS management
7. **ACM**: Add SSL/TLS certificates

## Security Notes

- Review and adjust security group rules based on your requirements
- Enable ALB deletion protection for production environments
- Configure HTTPS listener with ACM certificate for production
- Use AWS Secrets Manager for sensitive configuration
- Enable VPC flow logs for network monitoring

## Outputs

After applying, Terraform will output:

- `vpc_id`: ID of the created VPC
- `public_subnet_ids`: IDs of public subnets
- `private_subnet_ids`: IDs of private subnets
- `load_balancer_dns`: DNS name of the Application Load Balancer
- `security_group_id`: ID of the application security group

## Next Steps

1. Decide on deployment strategy (EC2, ECS, or EKS)
2. Add compute resources (EC2 instances or ECS tasks)
3. Configure application deployment mechanism
4. Set up CI/CD pipeline
5. Add monitoring and alerting
