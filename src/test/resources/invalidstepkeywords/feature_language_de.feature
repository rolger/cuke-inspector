# language: de
Funktionalität: Deutsche invalide Keywords

  Hintergrund:
    Gegeben sei this is invalid

  Szenario: No tags scenario
    Angenommen this is valid
    Aber this is invalid
    Dann this is valid

  Szenario: No tags scenario
    Gegeben seien this is invalid
    Angenommen this is valid
    Dann this is valid