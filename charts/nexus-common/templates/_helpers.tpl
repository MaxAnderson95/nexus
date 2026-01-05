{{/*
Expand the name of the chart.
*/}}
{{- define "nexus-common.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "nexus-common.fullname" -}}
{{- default .Release.Name .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "nexus-common.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "nexus-common.labels" -}}
helm.sh/chart: {{ include "nexus-common.chart" . }}
app.kubernetes.io/part-of: nexus-station
app.kubernetes.io/instance: {{ .Release.Name }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Values.managedBy | default "Helm" }}
{{- end }}

{{/*
Selector labels for a component
Usage: {{ include "nexus-common.selectorLabels" (dict "context" . "component" "power") }}
*/}}
{{- define "nexus-common.selectorLabels" -}}
app.kubernetes.io/name: {{ .component }}
app.kubernetes.io/instance: {{ .context.Release.Name }}
{{- end }}

{{/*
Generate the image path for a component.
Usage: {{ include "nexus-common.image" (dict "context" . "image" .Values.image) }}
*/}}
{{- define "nexus-common.image" -}}
{{- $registry := .image.registry | default "" -}}
{{- $repository := .image.repository -}}
{{- $tag := "" -}}
{{- if .image.tag -}}
{{- $tag = .image.tag -}}
{{- else if .context.Chart.AppVersion -}}
{{- $tag = .context.Chart.AppVersion -}}
{{- else -}}
{{- fail "image tag must be specified either via .image.tag in values.yaml or appVersion in Chart.yaml" -}}
{{- end -}}
{{- if $registry -}}
{{- printf "%s/%s:%s" $registry $repository $tag -}}
{{- else -}}
{{- printf "%s:%s" $repository $tag -}}
{{- end -}}
{{- end }}

{{/*
Generate image pull secrets
Usage: {{ include "nexus-common.imagePullSecrets" . }}
*/}}
{{- define "nexus-common.imagePullSecrets" -}}
{{- if .Values.imagePullSecrets }}
imagePullSecrets:
{{- range .Values.imagePullSecrets }}
  - name: {{ . }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Generate fully qualified service URL for cross-namespace communication.
If namespace is empty, uses short service name (same namespace).
Usage: {{ include "nexus-common.serviceUrl" (dict "service" "power" "namespace" "nexus-power" "port" 80) }}
*/}}
{{- define "nexus-common.serviceUrl" -}}
{{- if .namespace -}}
{{- printf "http://%s.%s.svc.cluster.local:%d" .service .namespace (int .port) -}}
{{- else -}}
{{- printf "http://%s:%d" .service (int .port) -}}
{{- end -}}
{{- end }}

{{/*
Generate PostgreSQL JDBC connection URL for cross-namespace access.
If namespace is empty, uses short service name (same namespace).
Usage: {{ include "nexus-common.postgresJdbcUrl" (dict "service" "postgres" "namespace" "nexus-infra" "port" 5432 "database" "nexus") }}
*/}}
{{- define "nexus-common.postgresJdbcUrl" -}}
{{- if .namespace -}}
{{- printf "jdbc:postgresql://%s.%s.svc.cluster.local:%d/%s" .service .namespace (int .port) .database -}}
{{- else -}}
{{- printf "jdbc:postgresql://%s:%d/%s" .service (int .port) .database -}}
{{- end -}}
{{- end }}

{{/*
Generate Redis host for cross-namespace access.
If namespace is empty, uses short service name (same namespace).
Usage: {{ include "nexus-common.redisHost" (dict "service" "redis" "namespace" "nexus-infra") }}
*/}}
{{- define "nexus-common.redisHost" -}}
{{- if .namespace -}}
{{- printf "%s.%s.svc.cluster.local" .service .namespace -}}
{{- else -}}
{{- .service -}}
{{- end -}}
{{- end }}

{{/*
Common environment variables for all services (pod metadata)
*/}}
{{- define "nexus-common.podEnv" -}}
- name: POD_NAME
  valueFrom:
    fieldRef:
      fieldPath: metadata.name
- name: POD_NAMESPACE
  valueFrom:
    fieldRef:
      fieldPath: metadata.namespace
- name: POD_IP
  valueFrom:
    fieldRef:
      fieldPath: status.podIP
- name: NODE_NAME
  valueFrom:
    fieldRef:
      fieldPath: spec.nodeName
{{- end }}

{{/*
Database environment variables
Usage: {{ include "nexus-common.databaseEnv" . }}
Expects .Values.global.namespaces.infra, .Values.global.services.postgres, .Values.global.ports.postgres
and .Values.database.name, .Values.database.username, .Values.database.password
*/}}
{{- define "nexus-common.databaseEnv" -}}
- name: SPRING_DATASOURCE_URL
  value: {{ include "nexus-common.postgresJdbcUrl" (dict
    "service" .Values.global.services.postgres
    "namespace" .Values.global.namespaces.infra
    "port" .Values.global.ports.postgres
    "database" .Values.database.name) | quote }}
- name: SPRING_DATASOURCE_USERNAME
  value: {{ .Values.database.username | quote }}
- name: SPRING_DATASOURCE_PASSWORD
  value: {{ .Values.database.password | quote }}
{{- end }}

{{/*
Redis environment variables
Usage: {{ include "nexus-common.redisEnv" . }}
Expects .Values.global.namespaces.infra, .Values.global.services.redis, .Values.global.ports.redis
*/}}
{{- define "nexus-common.redisEnv" -}}
- name: SPRING_DATA_REDIS_HOST
  value: {{ include "nexus-common.redisHost" (dict
    "service" .Values.global.services.redis
    "namespace" .Values.global.namespaces.infra) | quote }}
- name: SPRING_DATA_REDIS_PORT
  value: {{ .Values.global.ports.redis | quote }}
{{- end }}

{{/*
JSON logging environment variable
Usage: {{ include "nexus-common.loggingEnv" . }}
*/}}
{{- define "nexus-common.loggingEnv" -}}
{{- if .Values.enableJSONLogging }}
- name: LOGGING_STRUCTURED_FORMAT_CONSOLE
  value: "logstash"
{{- end }}
{{- end }}

{{/*
OpenTelemetry environment variables
Three modes:
  1. Dash0 (default): enabled=false, sdkDisabled=false - No env vars, Dash0 operator injects them
  2. Disabled: sdkDisabled=true - Sets OTEL_SDK_DISABLED=true
  3. Manual: enabled=true - Full manual OTEL configuration
Usage: {{ include "nexus-common.otelEnv" . }}
*/}}
{{- define "nexus-common.otelEnv" -}}
{{- if .Values.otel.sdkDisabled }}
- name: OTEL_SDK_DISABLED
  value: "true"
{{- else if .Values.otel.enabled }}
- name: OTEL_EXPORTER_OTLP_ENDPOINT
  value: {{ .Values.otel.endpoint | quote }}
- name: OTEL_EXPORTER_OTLP_PROTOCOL
  value: {{ .Values.otel.protocol | quote }}
{{- if .Values.otel.insecure }}
- name: OTEL_EXPORTER_OTLP_INSECURE
  value: "true"
{{- end }}
{{- end }}
{{- end }}

{{/*
Chaos Engineering environment variable
Usage: {{ include "nexus-common.chaosEnv" . }}
Expects .Values.chaos.level
*/}}
{{- define "nexus-common.chaosEnv" -}}
- name: CHAOS
  value: {{ .Values.chaos.level | default "none" | quote }}
{{- end }}

{{/*
Dash0 Monitoring custom resource
Usage: {{ include "nexus-common.dash0Monitoring" . }}
Expects .Values.dash0Monitoring.enabled and .Values.dash0Monitoring.spec
*/}}
{{- define "nexus-common.dash0Monitoring" -}}
{{- if .Values.dash0Monitoring.enabled }}
apiVersion: operator.dash0.com/v1beta1
kind: Dash0Monitoring
metadata:
  name: dash0-monitoring-resource
  labels:
    {{- include "nexus-common.labels" . | nindent 4 }}
spec:
  {{- $defaultSpec := dict
    "logCollection" (dict "enabled" false)
    "filter" (dict
      "traces" (dict
        "span" (list
          "attributes[\"http.route\"] == \"/actuator/health\""
          "attributes[\"http.route\"] != nil and HasPrefix(attributes[\"http.route\"], \"/actuator/health/\")"
        )
      )
    )
  }}
  {{- $mergedSpec := merge (.Values.dash0Monitoring.spec | default dict) $defaultSpec }}
  {{- toYaml $mergedSpec | nindent 2 }}
{{- end }}
{{- end }}
