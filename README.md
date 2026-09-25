# 📸 Galerie — Application Android

Application galerie photo et vidéo moderne et fluide pour Android, entièrement sans publicité, avec une interface soignée inspirée de Samsung Gallery.

## ✨ Fonctionnalités
- 🖼️ **Grille de photos & vidéos** : Navigation fluide et rapide parmi tous vos médias avec tri chronologique.
- 📁 **Gestion des albums** : Visualisation organisée par dossiers et albums locaux.
- 🔍 **Visionneuse plein écran** : Zoom précis et gestes tactiles fluides avec prise en charge haute définition.
- 🎬 **Lecteur vidéo intégré** : Lecture directe de vos enregistrements vidéo.
- 🌓 **Thèmes sombre & clair** : Support complet du mode sombre et de Material You / Material Design 3.
- 🚫 **Zéro publicité & Respect de la vie privée** : Aucune publicité, aucun traqueur, aucune collecte de données.

## 📱 Captures d'écran
*(À venir)*

## 🚀 Comment obtenir l'APK

### Méthode 1 : Via GitHub Actions (recommandé)
1. Créer un nouveau dépôt sur GitHub
2. Téléverser tous les fichiers du projet (via drag & drop sur github.com ou via git)
3. Aller dans l'onglet "Actions" du dépôt
4. Cliquer sur le workflow "Build APK"
5. Attendre la fin du build (2-3 minutes)
6. Télécharger l'artefact "gallery-app-debug" qui contient le fichier APK

### Méthode 2 : Via Git en ligne de commande
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/VOTRE_UTILISATEUR/gallery-app.git
git push -u origin main
```

## 📲 Installation sur votre téléphone
1. Transférer le fichier `.apk` sur votre téléphone.
2. Ouvrir le fichier `.apk`.
3. Autoriser l'installation depuis des sources inconnues si le système Android le demande.
4. L'application est installée !

## 🔒 Permissions
L'application respecte rigoureusement votre vie privée :
- Requiert uniquement l'accès en lecture aux médias (`READ_MEDIA_IMAGES` et `READ_MEDIA_VIDEO` sur Android 13+, ou `READ_EXTERNAL_STORAGE` sur les versions plus anciennes d'Android) afin d'afficher votre galerie locale.
- **Aucune permission Internet** : aucune donnée ne quitte votre appareil, aucun suivi publicitaire ou analytique.

## 🛠️ Développement
- **Langage** : Kotlin
- **UI & Design** : Material Design 3
- **Chargement d'images** : Glide
- **Zoom interactif** : PhotoView
- **Compatibilité minimale** : Android 7.0 (API 24)

## 📄 Licence
Ce projet est distribué sous licence [MIT](LICENSE).
