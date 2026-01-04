{{/*
Inventory service specific helpers that extend nexus-common
*/}}

{{- define "nexus-inventory.image" -}}
{{- include "nexus-common.image" . -}}
{{- end }}

{{- define "nexus-inventory.labels" -}}
{{- include "nexus-common.labels" . }}
{{- end }}

{{- define "nexus-inventory.selectorLabels" -}}
{{- include "nexus-common.selectorLabels" . }}
{{- end }}

{{- define "nexus-inventory.imagePullSecrets" -}}
{{- include "nexus-common.imagePullSecrets" . }}
{{- end }}

{{/*
Generate docking service URL
*/}}
{{- define "nexus-inventory.dockingUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.docking "namespace" .Values.global.namespaces.docking "port" .Values.global.ports.services) -}}
{{- end }}

{{/*
Generate crew service URL
*/}}
{{- define "nexus-inventory.crewUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.crew "namespace" .Values.global.namespaces.crew "port" .Values.global.ports.services) -}}
{{- end }}
