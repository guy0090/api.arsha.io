server {
        server_name api.example.com;
        listen 80;

        proxy_read_timeout 480;
        proxy_connect_timeout 480;
        proxy_send_timeout 480;

        access_log /var/log/nginx/api-example-com-access.log combined;
        error_log /var/log/nginx/api-example-com-error.log warn;

        location / {
          proxy_set_header X-Real-IP $remote_addr;
          proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
          proxy_pass http://127.0.0.1:3000;
        }
}