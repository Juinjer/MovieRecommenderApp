FROM node:18

WORKDIR /backend

COPY package*.json ./

RUN npm install

COPY . .

ENV REC="host.docker.internal"

EXPOSE 9999

RUN npm run build

CMD ["npm", "start"]
