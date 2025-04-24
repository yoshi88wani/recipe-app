FROM node:18-alpine

WORKDIR /app

# 依存関係をコピー
COPY package.json package-lock.json ./

# 依存関係をインストール
RUN npm install

# ソースコードをコピー
COPY . .

# 開発モードで起動
CMD ["npm", "run", "dev"] 