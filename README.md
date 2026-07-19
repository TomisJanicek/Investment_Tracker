# Investment Tracker

Moderní Android aplikace pro správu investičního portfolia. Projekt slouží jako sandbox pro testování pokročilých architektonických vzorů, reaktivního programování a moderních UI prvků v Jetpack Compose.

---

## 🛠️ Tech Stack & Architektura

Aplikace důsledně dodržuje **Clean Architecture** a reaktivní tok dat. Cílem bylo vytvořit kód, který je snadno udržovatelný, testovatelný a připravený na případné rozšiřování o další moduly.

-   **MVI (Model-View-Intent):** Každá obrazovka má svůj jasně definovaný stav a akce, což eliminuje nepředvídatelné chování UI.
-   **Kotlin Coroutines & Flow:** Kompletní reaktivní pipeline od databáze až po UI. Využití operátorů jako `combine` pro real-time výpočty portfolia.
-   **Jetpack Compose:** Deklarativní UI s Material 3 komponentami, vlastními pickery a animacemi.
-   **Dagger Hilt:** Dependency Injection pro čisté oddělení vrstev.
-   **Room Database:** Lokální perzistence s důrazem na datovou integritu a přesnost (použití `BigDecimal` pro peněžní částky).

---

## 📈 Funkcionality

-   **Reaktivní Portfolio:** Dashboard se okamžitě přepočítává při jakékoli změně v transakcích nebo cenách bez nutnosti manuálního refreshe.
-   **Správa aktiv:** Normalizovaná databáze tickerů s vazbou na historii obchodů.
-   **Cenová historie:** Možnost sledovat a editovat vývoj tržní ceny v čase pro každé aktivum zvlášť.
-   **Moderní UX:** Integrace `ModalBottomSheet` pro editační flow a Lottie animace pro potvrzení úspěšných akcí.
-   **Vizuální identita:** Vlastní paleta (Midnight Navy & Muted Gold) na pergamenovém podkladu.

---

## 🚀 TODO / Roadmap

- [ ] **Synchronizace s API:** Nahrazení manuálního zadávání cen integrací s externím poskytovatelem tržních dat.
- [ ] **Grafy a vizualizace:** Implementace interaktivních grafů vývoje portfolia pomocí Compose Charts.
- [ ] **Více měn (Multi-currency):** Podpora pro nákupy v USD/EUR a automatický přepočet dle aktuálního kurzu ČNB.
- [ ] **Kategorie a filtrace:** Možnost rozdělit aktiva na akcie, krypto, ETF a sledovat alokaci portfolia.
- [ ] **Unit & UI Testy:** Zvýšení pokrytí kódu testy (zejména doménové logiky výpočtů).

---

## 🏗️ Jak projekt spustit
1.  Klonujte repozitář.
2.  Otevřete v nejnovějším Android Studiu.
3.  Sestavte projekt (všechny závislosti jsou konfigurovány v `libs.versions.toml`).
