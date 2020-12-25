# How to run application

## Use MinIO as storage
1. Start minio container:
````
cd minio_docker
docker-compose up
````

2. Start application (default profile for MinIO)
````
mvn spring-boot:run
````


## Use AWS S3 as storage
1. To use aws s3 storage (need to set correct credentials using environment variables):

2. Start application (s3 profile will for AWS S3)
````
mvn spring-boot:run -Dspring-boot.run.profiles=s3
````
