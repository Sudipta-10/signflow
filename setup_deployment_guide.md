# Deployment Guide

This document outlines the steps required to deploy the "Verifile" (formerly Signflow) application, consisting of a Java Spring Boot backend and a Next.js frontend.
We will deploy the **Backend on Render** and the **Frontend on Vercel**.

## 1. Preparing for Deployment

Before you start deploying, make sure you have:
1. Pushed your latest code to a GitHub repository.
2. Created accounts on [Render.com](https://render.com) (for backend) and [Vercel.com](https://vercel.com) (for frontend).
3. Ready your environment variables (Database URLs, API keys, etc.).

---

## 2. Deploying the Spring Boot Backend to Render

Render is a great platform for hosting Java applications using Docker or Web Services natively. We will deploy the backend as a Web Service.

### Step 2.1: Create a `Dockerfile` (Recommended)
Since Render uses Docker for deploying Spring Boot efficiently, create a file named `Dockerfile` inside the `DocsSignatureAppBE` folder with the following content:

```dockerfile
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Step 2.2: Setup Render Web Service
1. Go to your Render Dashboard and click **New +** -> **Web Service**.
2. Connect your GitHub repository.
3. In the setup form:
   - **Name**: `verifile-backend`
   - **Root Directory**: `DocsSignatureAppBE`
   - **Environment**: `Docker` (Render will automatically detect the `Dockerfile` in the root directory).
   - **Instance Type**: Select the Free tier or a paid tier based on your needs.
4. Add your **Environment Variables** (e.g., `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `FRONTEND_URL`).
5. Click **Create Web Service**. 

Wait for the deployment to finish. Once it's live, copy the Render URL (e.g., `https://verifile-backend.onrender.com`).

---

## 3. Deploying the Next.js Frontend to Vercel

Vercel is the creator of Next.js and provides the easiest deployment experience for it.

### Step 3.1: Update Frontend Environment Variables
In your frontend code (`verifile-frontend`), ensure that all API calls route to your backend URL. 
Usually, you configure this in a `.env.local` or `.env.production` file using a variable like `NEXT_PUBLIC_API_URL`.

### Step 3.2: Connect and Deploy on Vercel
1. Go to your [Vercel Dashboard](https://vercel.com/dashboard) and click **Add New** -> **Project**.
2. Import your GitHub repository.
3. In the "Configure Project" step:
   - **Project Name**: `verifile-frontend`
   - **Framework Preset**: `Next.js`
   - **Root Directory**: Click "Edit" and select either `signflow-frontend` or `verifile-frontend` (depending on which folder contains your latest Next.js code).
4. **Environment Variables**: Open the Environment Variables dropdown and add:
   - `NEXT_PUBLIC_API_URL`: Your Render backend URL (e.g., `https://verifile-backend.onrender.com`)
5. Click **Deploy**.

Vercel will build and deploy your Next.js frontend. Once complete, you will receive a live URL.

---

## 4. Post-Deployment Checks
1. **CORS:** Ensure your Spring Boot backend's CORS configuration allows requests from your new Vercel frontend URL. If you have `@CrossOrigin` annotations or a global `WebMvcConfigurer`, update the allowed origins.
2. **Database:** Make sure your backend in Render connects securely to your production database (you can also provision a PostgreSQL database directly on Render).
3. **Tests:** Open the Vercel URL in your browser and try logging in, uploading a document, and testing the core "Verifile" features.
