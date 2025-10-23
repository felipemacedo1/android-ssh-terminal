# ✅ Checklist de Verificação do CI/CD

## Pré-Push
- [ ] `app/build.gradle.kts` contém `composeOptions` com `kotlinCompilerExtensionVersion`
- [ ] `.github/workflows/build.yml` contém validação do Gradle wrapper
- [ ] Workflow cria `local.properties` automaticamente
- [ ] Jobs têm timeouts configurados
- [ ] Testes instrumentados usam `ubuntu-latest` em vez de `macos-latest`

## Pós-Push
- [ ] Build job inicia e completa com sucesso
- [ ] Testes unitários passam
- [ ] APK debug é gerado e uploaded
- [ ] Lint report é gerado (pode ter warnings)
- [ ] Testes instrumentados iniciam o emulador corretamente
- [ ] AVD é cacheado para próximas execuções

## Verificações de Performance
- [ ] Build job completa em < 10 minutos
- [ ] Testes instrumentados completam em < 20 minutos
- [ ] Cache do Gradle funciona (builds subsequentes mais rápidos)
- [ ] Cache do AVD funciona (setup do emulador mais rápido)

## Verificações de Segurança
- [ ] Gradle wrapper validation passou
- [ ] Sem secrets expostos nos logs
- [ ] Versões de actions são fixas (não `@latest`)

## Artefatos Gerados
- [ ] `app-debug.apk` disponível para download
- [ ] `test-results` com relatórios dos testes unitários
- [ ] `lint-report` com análise de código
- [ ] `instrumentation-test-results` (se testes instrumentados executaram)

## Em Caso de Falha

### Build falha
1. Verificar logs do step que falhou
2. Confirmar se `local.properties` foi criado
3. Verificar versão do Gradle e JDK

### Testes falham
1. Executar testes localmente
2. Verificar se há dependências faltando
3. Verificar compatibilidade de versões

### Emulador não inicia
1. Verificar logs do KVM setup
2. Tentar reduzir API level
3. Verificar se cache do AVD está corrompido (limpar cache)

### Timeout
1. Aumentar timeout do job
2. Verificar se há processos travados
3. Otimizar testes (dividir em grupos)
