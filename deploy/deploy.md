# Deployment
Describes a deployment on a normal Linux machine.

## Build
Build a package with all needed config files.
mvn clean package


## Deploy on remote server
Copy package to remote server, plus the env file template used two steps below
scp target/gitotg-0.5.0-SNAPSHOT.jar golf@192.168.10.93:/home/golf
scp deploy/gitotg.env.example golf@192.168.10.93:/home/golf


## Setup remote machine to run the application as service
### Setup user
sudo useradd --system --home /opt/gitotg --shell /usr/sbin/nologin gitotg


### Setup dir
sudo mkdir -p /opt/gitotg/{db,logs}
sudo cp gitotg-0.5.0-SNAPSHOT.jar /opt/gitotg/gitotg.jar
sudo chown -R gitotg:gitotg /opt/gitotg

### Setup config for the daemon run
sudo vi /etc/systemd/system/gitotg.service

### Setup credentials
application.yaml reads CLIENT_ID, CLIENT_SECRET, GITHUB_CLIENT_ID and GITHUB_CLIENT_SECRET from the
environment and has no defaults for them. Skip this step and the JVM dies on the unresolved
placeholders at startup, and Restart=on-failure retries it every 5s forever.
sudo cp gitotg.env.example /etc/gitotg.env
sudo vi /etc/gitotg.env
sudo chown root:root /etc/gitotg.env
sudo chmod 600 /etc/gitotg.env

systemd reads the file as root before dropping to the gitotg user, so 600 root:root is enough - the
gitotg user does not need to read it. See gitotg.env.example for the redirect URIs to register with
Google and GitHub.

Then uncomment this line in /etc/systemd/system/gitotg.service, or the file is never read:
EnvironmentFile=/etc/gitotg.env

### Make changes available
sudo systemctl daemon-reload

### Start service by hand
sudo systemctl start gitotg

### Check if the service is aup and running
sudo systemctl status gitotg
