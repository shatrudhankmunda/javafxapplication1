FROM eclipse-temurin:17-jdk

# Set environment variable to avoid JavaFX crashing in containers
ENV JAVAFX_VERSION=21
ENV PATH="/app:${PATH}"
ENV JAVA_TOOL_OPTIONS="-Dprism.order=sw"
ENV GSETTINGS_BACKEND=memory
WORKDIR /app

# Copy build jar and download JavaFX SDK
COPY target/*.jar app.jar
RUN apt-get update && apt-get install -y wget unzip libgtk-3-0 libx11-xcb1 libxrender1 libxtst6 libxi6 libxext6 libnss3 \
 && wget https://download2.gluonhq.com/openjfx/${JAVAFX_VERSION}/openjfx-${JAVAFX_VERSION}_linux-x64_bin-sdk.zip \
 && unzip openjfx-${JAVAFX_VERSION}_linux-x64_bin-sdk.zip -d /opt \
 && rm openjfx-${JAVAFX_VERSION}_linux-x64_bin-sdk.zip

ENV PATH="/opt/javafx-sdk-${JAVAFX_VERSION}/bin:${PATH}"
ENV JAVAFX_LIB="/opt/javafx-sdk-${JAVAFX_VERSION}/lib"

# Run the JavaFX app
CMD ["java", "--module-path", "/opt/javafx-sdk-21/lib", "--add-modules", "javafx.controls,javafx.fxml", "--add-exports=javafx.graphics/com.sun.javafx.util=ALL-UNNAMED", "--add-exports=javafx.fxml/com.sun.javafx.fxml=ALL-UNNAMED", "-jar", "app.jar"]
