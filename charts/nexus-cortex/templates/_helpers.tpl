{{/*
Cortex service specific helpers that extend nexus-common
*/}}

{{- define "nexus-cortex.image" -}}
{{- include "nexus-common.image" . -}}
{{- end }}

{{- define "nexus-cortex.labels" -}}
{{- include "nexus-common.labels" . }}
{{- end }}

{{- define "nexus-cortex.selectorLabels" -}}
{{- include "nexus-common.selectorLabels" . }}
{{- end }}

{{- define "nexus-cortex.imagePullSecrets" -}}
{{- include "nexus-common.imagePullSecrets" . }}
{{- end }}

{{/*
Generate power service URL
*/}}
{{- define "nexus-cortex.powerUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.power "namespace" .Values.global.namespaces.power "port" .Values.global.ports.services) -}}
{{- end }}

{{/*
Generate life-support service URL
*/}}
{{- define "nexus-cortex.lifeSupportUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.lifeSupport "namespace" .Values.global.namespaces.lifeSupport "port" .Values.global.ports.services) -}}
{{- end }}

{{/*
Generate crew service URL
*/}}
{{- define "nexus-cortex.crewUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.crew "namespace" .Values.global.namespaces.crew "port" .Values.global.ports.services) -}}
{{- end }}

{{/*
Generate docking service URL
*/}}
{{- define "nexus-cortex.dockingUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.docking "namespace" .Values.global.namespaces.docking "port" .Values.global.ports.services) -}}
{{- end }}

{{/*
Generate inventory service URL
*/}}
{{- define "nexus-cortex.inventoryUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.inventory "namespace" .Values.global.namespaces.inventory "port" .Values.global.ports.services) -}}
{{- end }}
