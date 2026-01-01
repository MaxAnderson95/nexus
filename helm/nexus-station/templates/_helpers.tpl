{{/*
Expand the name of the chart.
*/}}
{{- define "nexus-station.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "nexus-station.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
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
{{ include "nexus-station.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "nexus-station.selectorLabels" -}}
app.kubernetes.io/name: {{ include "nexus-station.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name for a component
*/}}
{{- define "nexus-station.componentName" -}}
{{- $fullname := include "nexus-station.fullname" .root -}}
{{- printf "%s-%s" $fullname .component | trunc 63 | trimSuffix "-" -}}
{{- end }}

{{/*
Component labels
*/}}
{{- define "nexus-station.componentLabels" -}}
{{ include "nexus-station.labels" .root }}
app.kubernetes.io/component: {{ .component }}
{{- end }}

{{/*
Component selector labels
*/}}
{{- define "nexus-station.componentSelectorLabels" -}}
{{ include "nexus-station.selectorLabels" .root }}
app.kubernetes.io/component: {{ .component }}
{{- end }}

{{/*
Create image pull secrets
*/}}
{{- define "nexus-station.imagePullSecrets" -}}
{{- if .Values.global.imagePullSecrets }}
imagePullSecrets:
{{- range .Values.global.imagePullSecrets }}
  - name: {{ . }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create full image name
*/}}
{{- define "nexus-station.image" -}}
{{- $registry := .root.Values.global.imageRegistry -}}
{{- if $registry -}}
{{- printf "%s/%s:%s" $registry .image.repository .image.tag -}}
{{- else -}}
{{- printf "%s:%s" .image.repository .image.tag -}}
{{- end -}}
{{- end }}

{{/*
PostgreSQL connection URL
*/}}
{{- define "nexus-station.postgresUrl" -}}
{{- $host := printf "%s-postgres" (include "nexus-station.fullname" .) -}}
{{- printf "jdbc:postgresql://%s:%d/%s" $host (int .Values.postgresql.service.port) .Values.postgresql.auth.database -}}
{{- end }}

{{/*
Redis connection URL
*/}}
{{- define "nexus-station.redisUrl" -}}
{{- $host := printf "%s-redis" (include "nexus-station.fullname" .) -}}
{{- printf "redis://%s:%d" $host (int .Values.redis.service.port) -}}
{{- end }}
