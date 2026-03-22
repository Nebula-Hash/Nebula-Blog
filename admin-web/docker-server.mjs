import { createReadStream, existsSync, statSync } from 'node:fs'
import { readFile } from 'node:fs/promises'
import http from 'node:http'
import { extname, join, normalize } from 'node:path'

const port = Number(process.env.PORT || 80)
const backendHost = process.env.BACKEND_HOST || 'backend'
const backendPort = Number(process.env.BACKEND_PORT || 8081)
const distDir = '/app/dist'

const mimeTypes = {
  '.css': 'text/css; charset=utf-8',
  '.gif': 'image/gif',
  '.html': 'text/html; charset=utf-8',
  '.ico': 'image/x-icon',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.svg': 'image/svg+xml',
  '.txt': 'text/plain; charset=utf-8',
  '.webp': 'image/webp',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2'
}

function proxyRequest(req, res) {
  const proxy = http.request(
    {
      hostname: backendHost,
      port: backendPort,
      path: req.url,
      method: req.method,
      headers: {
        ...req.headers,
        host: req.headers.host || `${backendHost}:${backendPort}`,
        connection: 'close'
      }
    },
    (proxyRes) => {
      res.writeHead(proxyRes.statusCode || 502, proxyRes.headers)
      proxyRes.pipe(res)
    }
  )

  proxy.on('error', () => {
    res.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' })
    res.end(JSON.stringify({ message: 'Bad Gateway' }))
  })

  req.pipe(proxy)
}

async function serveStatic(req, res) {
  if (req.url === '/healthz') {
    res.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' })
    res.end('ok')
    return
  }

  if ((req.url || '').startsWith('/api/')) {
    proxyRequest(req, res)
    return
  }

  const urlPath = req.url === '/' ? '/index.html' : req.url || '/index.html'
  const safePath = normalize(decodeURIComponent(urlPath)).replace(/^(\.\.[/\\])+/, '')
  let filePath = join(distDir, safePath)

  if (!existsSync(filePath) || (existsSync(filePath) && statSync(filePath).isDirectory())) {
    filePath = join(distDir, 'index.html')
  }

  try {
    const ext = extname(filePath)
    const contentType = mimeTypes[ext] || 'application/octet-stream'

    if (ext === '.html') {
      const html = await readFile(filePath)
      res.writeHead(200, { 'Content-Type': contentType })
      res.end(html)
      return
    }

    res.writeHead(200, { 'Content-Type': contentType })
    createReadStream(filePath).pipe(res)
  } catch {
    res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' })
    res.end('Not Found')
  }
}

http.createServer(serveStatic).listen(port, '0.0.0.0', () => {
  console.log(`admin-web server listening on ${port}`)
})
