FROM docker.elastic.co/beats/filebeat:8.14.1

# Copia o arquivo de configuração para dentro do container
COPY filebeat.yml /usr/share/filebeat/filebeat.yml

# Executa o Filebeat como root
USER root
