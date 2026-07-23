# Deployment
Describes a deployment on a normal Linux machine.

## Build
Build a package with all needed config files.
mvn clean package


## Deploy on remote server
Copy package to remote server
scp target/gitotg-0.2.0-SNAPSHOT.jar root@159.223.29.176:/root/

## Setup remote machine to run the application as service
Setup dir
sudo mkdir -p /opt/gitotg/{db,logs}
sudo cp gitotg-0.2.0-SNAPSHOT.jar /opt/gitotg/gitotg.jar
sudo chown -R gitotg:gitotg /opt/gitotg

Setup user
sudo useradd --system --home /opt/gitotg --shell /usr/sbin/nologin gitotg

Setup config for the daemon run
sudo vi /etc/systemd/system/gitotg.service

Make changes available
systemctl daemon-reload

Start service by hand
sudo systemctl start gitotg

Check if the service is aup and running
sudo systemctl status gitotg
