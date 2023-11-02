package ca.ulaval.glo4002.codereview.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*


@State(
    name = "ca.ulaval.glo4002.codereview.settings",
    storages = [
        Storage("CodeReviewSettingsPersistence.xml", roamingType = RoamingType.DEFAULT, exportable = true)
    ],
    category = SettingsCategory.TOOLS,
)
@Service
class SettingsPersistence : PersistentStateComponent<SettingsPersistence.State> {
    companion object {
        fun getInstance(): SettingsPersistence {
            return ApplicationManager.getApplication().getService(SettingsPersistence::class.java)
        }
    }

    private var state: State = State(DEFAULT_RULES)

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun updateRules(predefinedRules: List<String>) {
        this.state.predefinedRules = predefinedRules
    }
    
    fun getRules(): List<String> {
        return this.state.predefinedRules
    }

    data class State(var predefinedRules: List<String> = emptyList())
}

val DEFAULT_RULES = mutableListOf<String>().apply {
    add("Commentaire inapproprié [C1]");
    add("Commentaire désuet [C2]");
    add("Commentaire redondant [C3]");
    add("Ce commentaire exprime mal son intention [C4]");
    add("Code commenté / dead code [C5]");
    add("Projet trop compliqué à exécuter [E1]");
    add("Tests trop compliqués à exécuter [E2]");
    add("Fonction avec trop d'arguments [F1]");
    add("Utilisation injustifiée d'arguments de sortie [F2]");
    add("Paramètre de type 'flag' [F3]");
    add("Méthode/fonction inutilisée [F4]");
    add("Plusieurs langages dans un seul fichier [G1]");
    add("Comportement espéré qui n'est pas implanté [G2]");
    add("Mauvais comportement pour les cas limites [G3]");
    add("Méthodes de garde supprimées par héritage [G4]");
    add("Duplication [G5]");
    add("Code au mauvais niveau d'abstraction [G6]");
    add("Classe parente qui dépend de ses classes filles [G7]");
    add("Surplus d'informations [G8]");
    add("Dead code [G9]");
    add("Espacement vertical inapproprié [G10]");
    add("Inconsistance [G11]");
    add("Code qui ne fait qu'ajouter du bruit (code cluttering) [G12]");
    add("Couplage artificiel [G13]");
    add("Feature envy / violation du tell don't as [G14]");
    add("Paramètre de type 'selecteur' [G15]");
    add("Incompréhensible / difficile à lire [G16]");
    add("Responsabilité mal placée [G17]");
    add("Utilisation du static inapproprié [G18]");
    add("Nom de variable pas suffisamment clair [G19]");
    add("Le nom de la fonction devrait dire ce que fait la fonction [G20]");
    add("Algorithme mal compris [G21]");
    add("Rendre physique cette dépendance logique [G22]");
    add("Utilisez du polymorphisme [G23]");
    add("Non conforme aux standards [G24]");
    add("Magic number / magic string [G25]");
    add("Manque de précision [G26]");
    add("Privilégiez la structure plutôt que les conventions [G27]");
    add("Condition complexe - extraire en méthode [G28]");
    add("Évitez les conditions négatives lorsque possible [G29]");
    add("Cette fonction ne devrait faire qu'une seule chose [G30]");
    add("Couplage temporaire caché [G31]");
    add("Évitez les choix arbitraires [G32]");
    add("Encapsulez les conditions limites [G33]");
    add("Une fonction ne devrait utiliser qu'un seul niveau d'abstraction [G34]");
    add("Gardez les configurations dans les classes de haut niveau [G35]");
    add("Violation de la loi de Demeter / train wreck [G36]");
    add("Utilisez un * pour vous éviter trop d'import [J1]");
    add("Ne cachez pas les constantes dans des interfaces [J2]");
    add("Utilisez plutôt un enum [J3]");
    add("Utilisez un nom plus descriptif [N1]");
    add("Utilisez les bons noms selon le niveau d'abstraction [N2]");
    add("Utilisez les noms standards si possible [N3]");
    add("Nom ambigü [N4]");
    add("Ajustez la longueur des noms de variables selon leur portée [N5]");
    add("Évitez les encodates du type m_ [N6]");
    add("Les noms doivent décrire les effets de bord [N7]");
    add("Manque de tests [T1]");
    add("Utilisez un outil de code coverage pour voir ce qu'il reste à tester [T2]");
    add("Testez tout, même si c'est trivial [T3]");
    add("N'ignorez pas de tests [T4]");
    add("Manque de tests pour les cas limites [T5]");
    add("Ajoutez plus de tests près des bugs précédents / zones à risque [T6]");
    add("Les 'patterns' des tests rouges en disent long [T7]");
    add("Les 'patterns' de la couverture des tests en disent long [T8]");
    add("Les tests unitaires devraient être rapides [T9]");
    add("Ligne inutilement vide - effacez");
    add("Ce test est inutile - effacez");
    add("Utilisez des exceptions au lieu de gérer des codes d'erreur");
    add("TODO - effacez le et règlez le plutôt !");
    add("Object non mocké - dans un test unitaire, il faut mocker toute dépendance");
    add("Ce test n'est pas unitaire");
}.toList()
