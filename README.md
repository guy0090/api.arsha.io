## BDO Market API 

---

### Credits

- Thank you to [Velia Inn](https://developers.veliainn.com) for initially documenting the endpoints
- Thank you to [BDO Codex](https://bdocodex.com) for providing all item data

### What is this?

A self-hostable wrapper to proxy and cache requests from Pearl Abyss' unofficial Black Desert Online Market API.

### Why?

The intention behind this project is to avoid spamming PA's servers with unwanted requests, as the endpoints documented are not done so by PA officially, but by members of the community.
To avoid putting an unnecessary load on PA's infrastructure, you can use this wrapper to cache requests and serve them from your own server.

### How do I use it?

You can use the public instance at https://dev.api.arsha.io, or you can host your own instance. Documentation on all available
endpoints is available on [Postman](https://documenter.getpostman.com/view/4028519/2s9Y5YRhp4#674b362e-2a27-4961-ac07-dfb925aee842).

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/4028519-0539c251-5838-4d15-94cd-565aa8293137?action=collection%2Ffork&source=rip_markdown&collection-url=entityId%3D4028519-0539c251-5838-4d15-94cd-565aa8293137%26entityType%3Dcollection%26workspaceId%3D69d535c0-8852-452e-81a7-f8c8584e19f0#?env%5BCONSOLE_NA%5D=W3sia2V5IjoicmVnaW9uIiwidmFsdWUiOiJjb25zb2xlX25hIiwiZW5hYmxlZCI6dHJ1ZX1d)

### How do I host my own instance?

You can host your own instance by cloning this repository and running a few commands. The only requirement is
that you have Docker installed on your machine. https://docs.docker.com/get-docker/

Ideally you would also have it hosted behind a reverse proxy such as Nginx or Caddy, but this is not a requirement. 
- [nginx example](nginx/api.example.com)

#### Steps:
```bash
git clone https://github.com/guy0090/api.arsha.io.git
cd api.arsha.io

# Optionally, you can change the port (default: 3000) that the API runs on by editing the exposed port in compose.yaml
vim compose.yaml

# Optionally, you can also configure API default properties:
cp config/application-default.yaml config/application.yaml
vim config/application.yaml

# Build and start container
docker compose up -d
```