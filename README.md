# Twitter Analytics Webservices

# Steps to build up the whole system

## 1. create cluster
```
cd phase3-deployment/cluster
eksctl create cluster -f cluster.yaml
```

## 2. create IAM policy if not created before
```
curl -o iam-policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/main/docs/install/iam_policy.json
aws iam create-policy --policy-name AWSLoadBalancerControllerIAMPolicy --policy-document file://iam-policy.json
```

## 3. install elb 
```
cd phase3-deployment/ingress

CLUSTER_NAME=demo-cluster
TEAM_AWS_ID=576651666672

eksctl utils associate-iam-oidc-provider --region us-east-1 --cluster $CLUSTER_NAME --approve
eksctl create iamserviceaccount --cluster=$CLUSTER_NAME --namespace=kube-system --name=aws-load-balancer-controller --attach-policy-arn=arn:aws:iam::$TEAM_AWS_ID:policy/AWSLoadBalancerControllerIAMPolicy --override-existing-serviceaccounts --approve

kubectl apply -k "github.com/aws/eks-charts/stable/aws-load-balancer-controller//crds?ref=master"

helm repo add eks https://aws.github.io/eks-charts

helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller --set clusterName=$CLUSTER_NAME --set serviceAccount.create=false --set region=us-east-1 --set serviceAccount.name=aws-load-balancer-controller -n kube-system

kubectl create -f ingress.yaml
```

## 4. install RDS MySQL
find the security groups(under VPC/security groups), put the value into main.tf:vpc_security_group_ids
```
cd phase3-deployment/rds
terraform init
terraform apply
```


## 5. install microservices
```
cd phase3-deployment/helm
helm install qrcode ./qrcode
helm install blockchain ./blockchain
helm install twitter ./twitter
```

## 6. install metrics server (optional)
If you want to monitor the metrics of the cluster, please install metrics server using the following command.
```
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

## 7. delete EKS cluster & RDS
```
eksctl delete cluster --region=us-east-1 --name=$CLUSTER_NAME --wait
terraform destroy
```
