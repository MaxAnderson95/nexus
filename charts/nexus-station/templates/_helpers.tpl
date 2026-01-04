{{/*
Expand the name of the chart.
*/}}
{{- define "nexus-station.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "nexus-station.fullname" -}}
{{- default .Release.Name .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "nexus-station.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "nexus-station.labels" -}}
helm.sh/chart: {{ include "nexus-station.chart" . }}
app.kubernetes.io/part-of: {{ include "nexus-station.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Values.managedBy }}
{{- end }}

{{/*
Selector labels for a component
Usage: {{ include "nexus-station.selectorLabels" (dict "context" . "component" "cortex") }}
*/}}
{{- define "nexus-station.selectorLabels" -}}
app.kubernetes.io/name: {{ .component }}
app.kubernetes.io/instance: {{ .context.Release.Name }}
{{- end }}

{{/*
Generate the image path for a component.
Usage: {{ include "nexus-station.image" (dict "context" . "image" .Values.cortex.image) }}
*/}}
{{- define "nexus-station.image" -}}
{{- $registry := .image.registry | default "" -}}
{{- $repository := .image.repository -}}
{{- $tag := .image.tag | default .context.Chart.AppVersion -}}
{{- if $registry -}}
{{- printf "%s/%s:%s" $registry $repository $tag -}}
{{- else -}}
{{- printf "%s:%s" $repository $tag -}}
{{- end -}}
{{- end }}

{{/*
Generate image pull secrets
Usage: {{ include "nexus-station.imagePullSecrets" . }}
*/}}
{{- define "nexus-station.imagePullSecrets" -}}
{{- if .Values.imagePullSecrets }}
imagePullSecrets:
{{- range .Values.imagePullSecrets }}
  - name: {{ . }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Generate PostgreSQL JDBC connection URL
*/}}
{{- define "nexus-station.postgresJdbcUrl" -}}
{{- printf "jdbc:postgresql://%s-postgres:%d/%s" (include "nexus-station.fullname" .) (int .Values.postgres.service.port) .Values.postgres.auth.database -}}
{{- end }}

{{/*
Common environment variables for all services
*/}}
{{- define "nexus-station.commonEnv" -}}
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
{{- if .Values.postgres.enabled }}
- name: SPRING_DATASOURCE_URL
  value: {{ include "nexus-station.postgresJdbcUrl" . | quote }}
- name: SPRING_DATASOURCE_USERNAME
  value: {{ .Values.postgres.auth.username | quote }}
- name: SPRING_DATASOURCE_PASSWORD
  value: {{ .Values.postgres.auth.password | quote }}
{{- end }}
{{- if .Values.redis.enabled }}
- name: SPRING_DATA_REDIS_HOST
  value: "{{ include "nexus-station.fullname" . }}-redis"
- name: SPRING_DATA_REDIS_PORT
  value: {{ .Values.redis.service.port | quote }}
{{- end }}
{{- end }}

{{/*
OpenTelemetry environment variables
Three modes:
  1. Dash0 (default): enabled=false, sdkDisabled=false - No env vars, Dash0 operator injects them
  2. Disabled: sdkDisabled=true - Sets OTEL_SDK_DISABLED=true
  3. Manual: enabled=true - Full manual OTEL configuration
*/}}
{{- define "nexus-station.otelEnv" -}}
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
Usage: {{ include "nexus-station.chaosEnv" (dict "context" . "serviceOverride" .Values.chaos.power) }}
Uses service-specific override if set, otherwise falls back to default
*/}}
{{- define "nexus-station.chaosEnv" -}}
- name: CHAOS
  value: {{ .serviceOverride | default .context.Values.chaos.default | quote }}
{{- end }}
