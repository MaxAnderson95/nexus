{{/*
Life Support service specific helpers that extend nexus-common
*/}}

{{- define "nexus-life-support.image" -}}
{{- include "nexus-common.image" . -}}
{{- end }}

{{- define "nexus-life-support.labels" -}}
{{- include "nexus-common.labels" . }}
{{- end }}

{{- define "nexus-life-support.selectorLabels" -}}
{{- include "nexus-common.selectorLabels" . }}
{{- end }}

{{- define "nexus-life-support.imagePullSecrets" -}}
{{- include "nexus-common.imagePullSecrets" . }}
{{- end }}

{{/*
Generate power service URL
*/}}
{{- define "nexus-life-support.powerUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.power "namespace" .Values.global.namespaces.power "port" .Values.global.ports.services) -}}
{{- end }}
