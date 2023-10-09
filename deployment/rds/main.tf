variable "aws_region" {
  default = "us-east-1"
}

provider "aws" {
  region = "${var.aws_region}"
}

resource "aws_db_subnet_group" "rds" {
  name       = "rds"
  subnet_ids = ["your eks vpc subnet ids here"]

  tags = {
        Project     = "twitter-phase-3"
  }
}

resource "aws_db_instance" "default" {
  availability_zone = "us-east-1a"
  apply_immediately = true
  identifier           = "twitter-database"
  allocated_storage    = 30
  storage_type         = "gp3"
  engine               = "mysql"
  engine_version       = "8.0.28"
  instance_class       = "db.m6g.xlarge"
  username             = "your db username here"
  password             = "your db password here"
  db_subnet_group_name = aws_db_subnet_group.rds.name
  vpc_security_group_ids = ["your vpc security group here (like sg-id)"]
  snapshot_identifier  = "your snapshot_identifier here (arn)"
  skip_final_snapshot  = true
  multi_az = false
  tags = {
        Project     = "twitter-phase-3"
  }
}