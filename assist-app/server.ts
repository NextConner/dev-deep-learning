import express from "express";
import { createServer as createViteServer } from "vite";

async function startServer() {
  const app = express();
  const PORT = 3000;

  // API routes
  app.get("/api/categories", (req, res) => {
    const categories = [
      { id: 'tech', name: '技术支持', icon: 'Cpu' },
      { id: 'business', name: '业务咨询', icon: 'Briefcase' },
      { id: 'hr', name: '行政人事', icon: 'Users' },
      { id: 'finance', name: '财务相关', icon: 'CreditCard' },
      { id: 'other', name: '其他', icon: 'MoreHorizontal' }
    ];
    res.json(categories);
  });

  // Vite middleware for development
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    app.use(express.static("dist"));
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on http://localhost:${PORT}`);
  });
}

startServer();
