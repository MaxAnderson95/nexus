{{/*
Load generator specific helpers that extend nexus-common
*/}}

{{- define "nexus-load-generator.image" -}}
{{- include "nexus-common.image" . -}}
{{- end }}

{{- define "nexus-load-generator.labels" -}}
{{- include "nexus-common.labels" . }}
{{- end }}

{{- define "nexus-load-generator.selectorLabels" -}}
{{- include "nexus-common.selectorLabels" . }}
{{- end }}

{{- define "nexus-load-generator.imagePullSecrets" -}}
{{- include "nexus-common.imagePullSecrets" . }}
{{- end }}

{{/*
Generate cortex service URL for load testing target
*/}}
{{- define "nexus-load-generator.cortexUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.cortex "namespace" .Values.global.namespaces.cortex "port" .Values.global.ports.services) -}}
{{- end }}
