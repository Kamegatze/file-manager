FROM nginx:stable
RUN apt update && apt -y install wget git libpcre3 libpcre3-dev gcc libssl-dev zlib1g zlib1g-dev make
RUN wget https://nginx.org/download/nginx-1.26.2.tar.gz && tar -xzvf nginx-1.26.2.tar.gz
RUN wget https://github.com/fdintino/nginx-upload-module/archive/refs/tags/2.3.0.tar.gz && tar -xzvf 2.3.0.tar.gz
WORKDIR /nginx-1.26.2
RUN ./configure --with-compat --add-dynamic-module=/nginx-upload-module-2.3.0 && make
RUN mv /nginx-1.26.2/objs/ngx_http_upload_module.so /usr/lib/nginx/modules/ && rm -r /nginx-1.26.2 && rm -r /nginx-upload-module-2.3.0
WORKDIR /
EXPOSE 80