# Login
docker login

# Build JAR
mvn clean package

# Build Docker image
docker build -t javafx-app .

# Run container
docker run --rm -it --name javafx-app -e DISPLAY=:0 javafx-app

# OR with docker-compose
docker-compose build
docker-compose up

# Push image (optional)
docker tag javafx-app your-dockerhub-username/javafx-app:latest
docker push your-dockerhub-username/javafx-app:latest
