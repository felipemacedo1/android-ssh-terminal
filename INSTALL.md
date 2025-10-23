# 📱 Como Instalar o KTAR

## 📥 Download

### Opção 1: Release Oficial (Recomendado)
1. Vá para a página de [Releases](https://github.com/felipemacedo1/ktar/releases)
2. Baixe o arquivo `app-debug.apk` da versão mais recente

### Opção 2: Build Manual
```bash
git clone https://github.com/felipemacedo1/ktar.git
cd ktar
./gradlew assembleDebug
```

## 🔧 Instalação no Android

### Pré-requisitos
- **Android 8.0** (API 26) ou superior
- **Espaço livre**: ~15 MB

### Passos para Instalação

1. **Baixe o APK** usando uma das opções acima

2. **Habilite fontes desconhecidas**:
   - Vá em **Configurações > Segurança**
   - Ative **Fontes desconhecidas** ou **Instalar apps desconhecidos**
   - Ou quando tentar instalar, o Android pedirá permissão

3. **Instale o app**:
   - Abra o arquivo `app-debug.apk` baixado
   - Toque em **Instalar**
   - Aguarde a instalação concluir

4. **Abra o aplicativo**:
   - Encontre "KTAR" na lista de apps
   - Toque para abrir

## 🔐 Verificação de Segurança

Este aplicativo é:
- ✅ **Open Source** - código totalmente auditável
- ✅ **Sem anúncios** - completamente gratuito
- ✅ **Sem rastreamento** - não coleta dados pessoais
- ✅ **Build automático** - compilado pelo GitHub Actions

## ❓ Problemas Comuns

### "App não instalado"
- Verifique se tem espaço suficiente (15+ MB)
- Certifique-se que "Fontes desconhecidas" está habilitado
- Tente reiniciar o dispositivo

### "Arquivo corrompido"
- Baixe o APK novamente
- Verifique sua conexão de internet durante o download

### App não abre
- Verifique se seu Android é 8.0+ (API 26)
- Reinicie o dispositivo
- Limpe o cache do app em Configurações

## 🆘 Suporte

Se encontrar problemas:
1. Verifique as [Issues](https://github.com/felipemacedo1/ktar/issues) existentes
2. Crie uma nova issue com detalhes do problema
3. Inclua modelo do dispositivo e versão do Android

---

**⚠️ Importante**: Este app requer permissões de rede para funcionar. Sempre verifique as chaves de host ao conectar a servidores pela primeira vez.