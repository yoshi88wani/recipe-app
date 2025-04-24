import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  swcMinify: true,
  output: 'standalone',
  
  // Docker環境用の設定
  experimental: {
    // ホットリロードの最適化
    optimizeCss: true,
    scrollRestoration: true,
  },
  
  // 開発サーバーの設定
  webpack(config: any, { dev, isServer }: { dev: boolean, isServer: boolean }) {
    // 開発モードの最適化
    if (dev && !isServer) {
      const originalEntry = config.entry;
      config.entry = async () => {
        const entries = await originalEntry();
        return entries;
      };
    }
    return config;
  },
  
  // APIプロキシ設定
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://backend:8080/api/:path*',
      },
    ];
  },
  
  // 画像ドメイン設定
  images: {
    domains: ['localhost', 'backend'],
  },
  
};

export default nextConfig;