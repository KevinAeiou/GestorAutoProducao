package com.kevin.gestorproducao.ui.fragment;

import static android.view.View.VISIBLE;

import static com.kevin.gestorproducao.utilitario.Utilitario.extrairDescricao;
import static com.kevin.gestorproducao.utilitario.Utilitario.extrairNivel;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.kevin.gestorproducao.R;
import com.kevin.gestorproducao.databinding.FragmentFiltroBottomSheetBinding;
import com.kevin.gestorproducao.model.FiltroTrabalho;
import com.kevin.gestorproducao.model.ProfissaoBase;
import com.kevin.gestorproducao.model.ProfissaoPersonagem;
import com.kevin.gestorproducao.ui.viewModel.FiltroViewModel;
import com.kevin.gestorproducao.ui.viewModel.PersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.ProfissaoPersonagemViewModel;
import com.kevin.gestorproducao.ui.viewModel.factory.ViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class FiltroBottomSheetFragment extends BottomSheetDialogFragment {
    private FragmentFiltroBottomSheetBinding binding;
    private FiltroViewModel filtroViewModel;
    private ProfissaoPersonagemViewModel profissaoPersonagemViewModel;
    private PersonagemViewModel personagemViewModel;
    private MaterialButton btnAplicar;
    private ImageView btnFechar;
    private TextInputEditText edtDescricaoFiltro;
    private ArrayList<ProfissaoPersonagem> profissoes;
    private ChipGroup chipGroupProfissoes, chipGroupRaridades, chipGroupEstados;
    private LinearLayout layoutEstados;
    private boolean ehProducao;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle argumento = getArguments();
        if (argumento != null) {
            FiltroBottomSheetFragmentArgs t = FiltroBottomSheetFragmentArgs.fromBundle(argumento);

            ehProducao = t.getEhProducao();
        }
    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {
        binding = FragmentFiltroBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializaComponentes();
        configuraLayoutEstado();
        configuraBotaoAplicar();
        configuraBotaoFechar();
        popularChipsRaridades();
        popularChipsEstados();
        preencherCampos();
        observarPersonagem();
        observarProfissoesPersonagem();
    }

    private void observarPersonagem() {
        personagemViewModel.pegaPersonagemSelecionado().observe(
            getViewLifecycleOwner(),
            personagem -> {
                if (personagem == null) return;

                profissaoPersonagemViewModel.setIdPersonagem(personagem.getId());
            }
        );
    }

    private void popularChipsEstados() {
        if (ehProducao) {
            chipGroupEstados.removeAllViews();

            List<String> estados = new ArrayList<>();
            estados.add(getString(R.string.stringFiltroParaProduzir));
            estados.add(getString(R.string.stringFiltroProduzindo));
            estados.add(getString(R.string.stringFiltroFeito));

            LayoutInflater inflater = LayoutInflater.from(requireContext());

            for (int i = 0; i < estados.size(); i++) {
                String estado = estados.get(i);

                Chip chip = (Chip) inflater.inflate(
                    R.layout.custom_chip,
                    chipGroupRaridades,
                    false
                );
                chip.setText(estado);
                chip.setCheckable(true);
                chip.setTag(i);

                chipGroupEstados.addView(chip);
            }
        }
    }

    private void configuraLayoutEstado() {
        if (ehProducao) {
            layoutEstados.setVisibility(VISIBLE);
        }
    }

    private void popularChipsRaridades() {
        chipGroupRaridades.removeAllViews();

        List<String> raridades = new ArrayList<>();
        raridades.add(getString(R.string.stringComum));
        raridades.add(getString(R.string.stringMelhorado));
        raridades.add(getString(R.string.stringRaro));
        raridades.add(getString(R.string.stringEspecial));

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (String raridade : raridades) {
            Chip chip = (Chip) inflater.inflate(
                R.layout.custom_chip,
                chipGroupRaridades,
            false
            );
            chip.setText(raridade);
            chip.setCheckable(true);

            chipGroupRaridades.addView(chip);
        }
    }

    private void observarProfissoesPersonagem() {
        profissaoPersonagemViewModel.getProfissoesPersonagem().observe(
            getViewLifecycleOwner(),
            resultado -> {
                if (resultado.getDado() != null) {
                    profissoes.clear();
                    profissoes.addAll(resultado.getDado());

                    popularChipsProfissoes();
                    preencherCampos();
                }
            }
        );
    }

    private void popularChipsProfissoes() {
        chipGroupProfissoes.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (ProfissaoPersonagem profissao : profissoes) {
            Chip chip = (Chip) inflater.inflate(
                R.layout.custom_chip,
                chipGroupProfissoes,
                false
            );

            chip.setText(profissao.getNome());
            chip.setCheckable(true);

            chip.setTag(profissao);

            chipGroupProfissoes.addView(chip);
        }
    }

    private void preencherCampos() {
        filtroViewModel.getFiltro().observe(
            getViewLifecycleOwner(),
            filtro -> {
                if (filtro == null || filtro.getProfissoes() == null) return;

                String textoNivel = filtro.getNivel() == null ? "" : filtro.getNivel().toString();
                String textoFiltro = filtro.getDescricao() + textoNivel;
                edtDescricaoFiltro.setText(textoFiltro);

                for (int i = 0; i < chipGroupProfissoes.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupProfissoes.getChildAt(i);
                    ProfissaoBase profissaoChip = (ProfissaoBase) chip.getTag();

                    boolean selecionado = filtro.getProfissoes().stream().anyMatch(
                        p -> p.getNome().equalsIgnoreCase(profissaoChip.getNome())
                    );

                    chip.setChecked(selecionado);
                }

                for (int i = 0; i < chipGroupRaridades.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupRaridades.getChildAt(i);
                    String raridade = (String) chip.getText();

                    boolean selecionado = filtro.getRaridades().stream().anyMatch(
                        r -> r.equalsIgnoreCase(raridade)
                    );

                    chip.setChecked(selecionado);
                }

                for (int i = 0; i < chipGroupEstados.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupEstados.getChildAt(i);
                    int estado = (int) chip.getTag();

                    if (estado == filtro.getEstado()) {
                        chip.setChecked(true);
                    }
                }
            }
        );
    }

    private void configuraBotaoFechar() {
        btnFechar.setOnClickListener(v -> dismiss());
    }

    private void configuraBotaoAplicar() {
        btnAplicar.setOnClickListener(v-> {
            String textoFiltro = edtDescricaoFiltro.getText().toString().trim();

            Integer nivel = extrairNivel(textoFiltro);
            String descricao = extrairDescricao(textoFiltro);

            List<ProfissaoBase> profissoesSelecionadas = new ArrayList<>();

            for (int i = 0; i < chipGroupProfissoes.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupProfissoes.getChildAt(i);

                if (chip.isChecked()) {
                    profissoesSelecionadas.add((ProfissaoBase) chip.getTag());
                }
            }

            List<String> raridadesSelecionadas = new ArrayList<>();

            for (int i = 0; i < chipGroupRaridades.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupRaridades.getChildAt(i);

                if (chip.isChecked()) {
                    raridadesSelecionadas.add((String) chip.getText());
                }
            }

            int estado = -1;

            for (int i = 0; i < chipGroupEstados.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupEstados.getChildAt(i);

                if (chip.isChecked()) {
                    estado = (int) chip.getTag();
                    break;
                }
            }

            boolean semFiltro =
                descricao.isEmpty() &&
                profissoesSelecionadas.isEmpty() &&
                raridadesSelecionadas.isEmpty() &&
                estado == -1 &&
                nivel == null;

            if (semFiltro) {
                filtroViewModel.aplicarFiltro(null);
            } else {
                FiltroTrabalho filtro = new FiltroTrabalho(
                    descricao,
                    profissoesSelecionadas,
                    raridadesSelecionadas,
                    estado,
                    nivel
                );
                filtroViewModel.aplicarFiltro(filtro);
            }

            dismiss();
        });
    }

    private void inicializaComponentes() {
        btnAplicar = binding.btnAplicarFiltroFragment;
        btnFechar = binding.btnFecharFiltroFragment;
        edtDescricaoFiltro = binding.edtDescricaoFiltroFragment;
        profissoes = new ArrayList<>();
        chipGroupProfissoes = binding.chipGroupProfissoes;
        chipGroupRaridades = binding.chipGroupRaridades;
        chipGroupEstados = binding.chipGroupEstados;
        layoutEstados = binding.layoutEstadoFiltroFragment;

        filtroViewModel = new ViewModelProvider(
            requireActivity()
        ).get(FiltroViewModel.class);

        ViewModelFactory viewModelFactory = new ViewModelFactory(getContext());
        personagemViewModel = new ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(PersonagemViewModel.class);

        profissaoPersonagemViewModel = new ViewModelProvider(
            this,
            viewModelFactory
        ).get(ProfissaoPersonagemViewModel.class);
    }

    // Achado M8: diferente da maioria dos fragments (que estendem BaseFragment e zeram o
    // binding aqui), este e o SelecaoPersonagemFragment estendem BottomSheetDialogFragment/
    // DialogFragment direto e nunca sobrescreviam onDestroyView — o binding vazava até o
    // fragment ser coletado.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}