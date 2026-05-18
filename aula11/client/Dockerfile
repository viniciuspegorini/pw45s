# # Fetching the latest node image on apline linux
# FROM node:alpine AS builder

# # Declaring env
# ENV NODE_ENV production

# # Setting up the work directory
# WORKDIR /app

# # Installing dependencies
# COPY ./package.json ./
# RUN npm install

# # Copying all the files in our project
# COPY . .

# # Building our application
# RUN npm run build

# # Fetching the latest nginx image
# FROM nginx

# # Copying built assets from builder
# COPY --from=builder /app/build /usr/share/nginx/html

# # Copying our nginx.conf
# COPY nginx.conf /etc/nginx/conf.d/default.conf

FROM node:18.15.0-alpine as build-step
RUN mkdir /app
WORKDIR /app
COPY package.json /app
RUN npm install
COPY . /app
RUN npm run build
FROM nginx:stable-alpine
COPY --from=build-step /app/dist /usr/share/nginx/html
EXPOSE 8100
CMD ["nginx", "-g", "daemon off;"]