-- Departments
INSERT INTO departments (department_name)
VALUES
  ('Computer Science'),
  ('Mechanical Engineering'),
  ('Physics'),
  ('Chemistry'),
  ('Business Administration');

-- COMPUTER SCIENCE (ID: 1)
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Scalable Byzantine Fault Tolerance in Permissioned Blockchains', 'Alice Smith, John Doe', 'As decentralized systems transition from public to private sectors, the need for scalable consensus mechanisms grows. This paper introduces a sharded Byzantine Fault Tolerance (BFT) protocol that achieves 50,000 transactions per second under 30% node failure scenarios. We provide a rigorous mathematical proof of safety and liveness.', '/files/cs/blockchain_scalability_final.pdf', 1, '2024-02-14', FALSE, NOW(), NOW()),
('Edge Computing: A Survey', 'Bob Zhang', 'Short overview of edge computing paradigms.', '/files/cs/edge_survey.pdf', 1, '2023-11-05', TRUE, '2023-11-05 09:00:00', NOW()),
('Generative Adversarial Networks for Synthetic Financial Data', 'Carol Johnson, Mark Specter', 'The scarcity of labeled financial data remains a bottleneck for machine learning in fintech. This research proposes "FinGAN," a generative model capable of synthesizing realistic stock market volatility patterns while strictly adhering to privacy-preserving constraints. Our results show that models trained on FinGAN data perform within 2% accuracy of those trained on real-world datasets.', '/files/cs/fin_gan_v2.pdf', 1, '2025-01-10', FALSE, NOW(), NOW()),
('On the Complexity of Non-Deterministic Polynomial Time', 'David Kim', 'A brief reconsideration of P vs NP boundaries in specific graph-theoretic constraints.', '/files/cs/p_vs_np_notes.pdf', 1, '2022-05-20', TRUE, '2022-05-20 14:00:00', NOW()),
('Human-Computer Interaction in Virtual Reality Surgical Simulators', 'Emma Turner, Sarah Vance', 'This study investigates the tactile feedback latency thresholds for surgeons using VR tools. By testing 50 resident surgeons, we identified that latencies above 15ms significantly degrade the precision of arterial suturing. We propose a predictive haptic rendering algorithm to mask network jitter.', '/files/cs/vr_surgery_haptics.pdf', 1, '2024-09-12', FALSE, NOW(), NOW());

-- MECHANICAL ENGINEERING (ID: 2)
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Topology Optimization of 3D Printed Aerospace Brackets', 'Frank Miller', 'Weight reduction in aerospace components is critical for fuel efficiency. This paper utilizes a density-based topology optimization approach to redesign standard titanium brackets. The resulting lattice structures provide a 40% weight reduction while maintaining a safety factor of 1.5.', '/files/me/topo_opt_aero.pdf', 2, '2024-03-22', FALSE, NOW(), NOW()),
('Thermal Management of High-Density Lithium-Ion Battery Packs', 'Grace Lee, Henry Ford III', 'Lithium-ion batteries are prone to thermal runaway. We present a hybrid cooling system combining phase-change materials (PCM) with active liquid cooling. Experimental results demonstrate a 15-degree Celsius reduction in peak temperature during fast-charging cycles (4C rate).', '/files/me/battery_thermal_2024.pdf', 2, '2024-06-18', FALSE, NOW(), NOW()),
('Micro-Actuator Design', 'Ishaan Kumar', 'Preliminary study on piezo-electric actuators for small-scale robotics.', '/files/me/micro_actuator_draft.pdf', 2, '2021-12-01', TRUE, '2021-12-01 10:00:00', NOW()),
('Aerodynamics of Flapping Wing Micro-Air Vehicles', 'Julia Morgan', 'Bio-inspired flight mechanisms.', '/files/me/mav_flapping.pdf', 2, '2024-11-20', FALSE, NOW(), NOW()),
('Fatigue Life Prediction of Welded Joints in Offshore Wind Turbines', 'Kevin Chen', 'Offshore structures face extreme cyclic loading. This paper reviews the S-N curve methodology versus fracture mechanics approaches for predicting the remaining useful life of welded tubular joints. We introduce a modified Paris Law model that accounts for saltwater corrosion rates.', '/files/me/wind_turbine_fatigue.pdf', 2, '2023-08-14', FALSE, NOW(), NOW());

-- PHYSICS (ID: 3)
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Muon g-2 Anomalies: A Path to New Physics?', 'Mohammed Salah', 'Recent measurements of the muon magnetic anomaly suggest a discrepancy with the Standard Model. This paper explores the possibility of a dark photon mediator explaining the 4.2-sigma deviation observed at Fermilab.', '/files/ph/muon_anomaly_study.pdf', 3, '2024-01-05', FALSE, NOW(), NOW()),
('High-Temperature Superconductivity in Hydrides', 'Oscar Fernandez', 'Observation of zero-resistance states in sulfur-hydride systems under pressures exceeding 150 GPa. While room temperature remains elusive, these findings pave the way for ambient-pressure applications via chemical pre-compression.', '/files/ph/supercond_hydride.pdf', 3, '2024-07-29', FALSE, NOW(), NOW()),
('Black Hole Information Paradox: A Firewall Perspective', 'Natalie Young', 'Does an observer falling into a black hole see a firewall? This theoretical analysis synthesizes the AMPS paradox with modern AdS/CFT correspondence theories to suggest that unitarity requires a radical rethinking of the event horizon.', '/files/ph/blackhole_firewall.pdf', 3, '2023-10-15', FALSE, NOW(), NOW()),
('Solar Flare Forecasting', 'Kevin Walker', 'Using X-ray flux data to predict M-class solar flares.', '/files/ph/solar_flares.pdf', 3, '2020-05-12', TRUE, '2020-05-12 11:00:00', NOW()),
('Neutrino Oscillation Parameters in Long-Baseline Experiments', 'Laura Palmer', 'Final results from the 5-year study on neutrino flavor mixing.', '/files/ph/neutrino_final.pdf', 3, '2024-12-01', FALSE, NOW(), NOW());

-- CHEMISTRY (ID: 4)
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Green Synthesis of Silver Nanoparticles using Eucalyptus Leaf Extract', 'Priya Singh, Anil Kapur', 'We report a cost-effective and eco-friendly method for the synthesis of silver nanoparticles (AgNPs). The plant metabolites act as both reducing and stabilizing agents. Characterization via UV-Vis and TEM confirms a spherical morphology with an average size of 20nm.', '/files/ch/green_nano_silver.pdf', 4, '2024-05-14', FALSE, NOW(), NOW()),
('Total Synthesis of Macrolide Antibiotics', 'Rachel Adams', 'An 18-step total synthesis of a novel macrolide.', '/files/ch/macrolide_synth.pdf', 4, '2023-02-10', TRUE, '2023-02-10 08:30:00', NOW()),
('Machine Learning in Retrosynthetic Analysis', 'Samuel Yeo', 'Can AI replace the chemist in route planning? We evaluate three transformer-based models on their ability to predict precursor molecules for complex natural products. Accuracy reached 78% for known reactions but dropped to 12% for novel bond-forming strategies.', '/files/ch/ml_retrosynthesis.pdf', 4, '2024-08-03', FALSE, NOW(), NOW()),
('Electrochemical Detection of Heavy Metals in Urban Waterways', 'Teresa van Dijk', 'Modified carbon paste electrodes for lead detection.', '/files/ch/heavy_metal_sensors.pdf', 4, '2024-10-10', FALSE, NOW(), NOW()),
('CO2 Capture via Metal-Organic Frameworks (MOFs)', 'Quentin Brooks', 'The climate crisis necessitates efficient carbon sequestration. This paper presents a novel MOF with an unprecedented surface area of 7000 m2/g, showing a 30% increase in CO2 adsorption capacity compared to HKUST-1.', '/files/ch/mof_carbon_capture.pdf', 4, '2024-12-15', FALSE, NOW(), NOW());

-- BUSINESS ADMINISTRATION (ID: 5)
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('The Gig Economy and Worker Well-being: A Longitudinal Study', 'Wendy Liu', 'As platform-based work becomes the norm, concerns regarding job security and mental health have surfaced. This three-year study of 2,000 Uber and DoorDash drivers suggests that while flexibility is valued, the lack of benefits leads to a 20% higher burnout rate compared to traditional employment.', '/files/ba/gig_economy_health.pdf', 5, '2024-04-11', FALSE, NOW(), NOW()),
('Corporate Social Responsibility or Greenwashing?', 'Victor Chang', 'Analysis of ESG reports from Fortune 500 companies.', '/files/ba/csr_vs_greenwashing.pdf', 5, '2023-09-30', FALSE, NOW(), NOW()),
('AI-Driven Dynamic Pricing in E-commerce', 'Uma Patel', 'How algorithms adjust prices in real-time.', '/files/ba/dynamic_pricing_v1.pdf', 5, '2022-01-15', TRUE, '2022-01-15 16:20:00', NOW()),
('The Impact of Remote Work on Organizational Culture', 'Xavier Perez, Yasmin Said', 'Is the "water cooler" conversation essential for innovation? Through a mixed-methods approach involving 15 tech firms, we found that while individual productivity rose by 10% during remote work, inter-departmental collaboration decreased by 25%.', '/files/ba/remote_work_culture.pdf', 5, '2024-06-25', FALSE, NOW(), NOW()),
('Venture Capital Trends in Southeast Asia', 'Lee Min-ho', 'A report on the shift from fintech to agritech investments in the ASEAN region.', '/files/ba/vc_trends_asean.pdf', 5, '2024-11-02', FALSE, NOW(), NOW());

-- COMPUTER SCIENCE (ID: 1)
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Scalable Byzantine Fault Tolerance in Permissioned Blockchains', 'Alice Smith, John Doe', 'As decentralized systems transition from public to private sectors, the need for scalable consensus mechanisms grows. This paper introduces a sharded Byzantine Fault Tolerance (BFT) protocol that achieves 50,000 transactions per second under 30% node failure scenarios. We provide a rigorous mathematical proof of safety and liveness.', '/files/cs/blockchain_scalability_final.pdf', 1, '2024-02-14', FALSE, NOW(), NOW()),
('Edge Computing: A Survey', 'Bob Zhang', 'Short overview of edge computing paradigms.', '/files/cs/edge_survey.pdf', 1, '2023-11-05', TRUE, '2023-11-05 09:00:00', NOW()),
('Generative Adversarial Networks for Synthetic Financial Data', 'Carol Johnson, Mark Specter', 'The scarcity of labeled financial data remains a bottleneck for machine learning in fintech. This research proposes "FinGAN," a generative model capable of synthesizing realistic stock market volatility patterns while strictly adhering to privacy-preserving constraints. Our results show that models trained on FinGAN data perform within 2% accuracy of those trained on real-world datasets.', '/files/cs/fin_gan_v2.pdf', 1, '2025-01-10', FALSE, NOW(), NOW()),
('On the Complexity of Non-Deterministic Polynomial Time', 'David Kim', 'A brief reconsideration of P vs NP boundaries in specific graph-theoretic constraints.', '/files/cs/p_vs_np_notes.pdf', 1, '2022-05-20', TRUE, '2022-05-20 14:00:00', NOW()),
('Human-Computer Interaction in Virtual Reality Surgical Simulators', 'Emma Turner, Sarah Vance', 'This study investigates the tactile feedback latency thresholds for surgeons using VR tools. By testing 50 resident surgeons, we identified that latencies above 15ms significantly degrade the precision of arterial suturing. We propose a predictive haptic rendering algorithm to mask network jitter.', '/files/cs/vr_surgery_haptics.pdf', 1, '2024-09-12', FALSE, NOW(), NOW());

-- MECHANICAL ENGINEERING (ID: 2)
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Topology Optimization of 3D Printed Aerospace Brackets', 'Frank Miller', 'Weight reduction in aerospace components is critical for fuel efficiency. This paper utilizes a density-based topology optimization approach to redesign standard titanium brackets. The resulting lattice structures provide a 40% weight reduction while maintaining a safety factor of 1.5.', '/files/me/topo_opt_aero.pdf', 2, '2024-03-22', FALSE, NOW(), NOW()),
('Thermal Management of High-Density Lithium-Ion Battery Packs', 'Grace Lee, Henry Ford III', 'Lithium-ion batteries are prone to thermal runaway. We present a hybrid cooling system combining phase-change materials (PCM) with active liquid cooling. Experimental results demonstrate a 15-degree Celsius reduction in peak temperature during fast-charging cycles (4C rate).', '/files/me/battery_thermal_2024.pdf', 2, '2024-06-18', FALSE, NOW(), NOW()),
('Micro-Actuator Design', 'Ishaan Kumar', 'Preliminary study on piezo-electric actuators for small-scale robotics.', '/files/me/micro_actuator_draft.pdf', 2, '2021-12-01', TRUE, '2021-12-01 10:00:00', NOW()),
('Aerodynamics of Flapping Wing Micro-Air Vehicles', 'Julia Morgan', 'Bio-inspired flight mechanisms.', '/files/me/mav_flapping.pdf', 2, '2024-11-20', FALSE, NOW(), NOW()),
('Fatigue Life Prediction of Welded Joints in Offshore Wind Turbines', 'Kevin Chen', 'Offshore structures face extreme cyclic loading. This paper reviews the S-N curve methodology versus fracture mechanics approaches for predicting the remaining useful life of welded tubular joints. We introduce a modified Paris Law model that accounts for saltwater corrosion rates.', '/files/me/wind_turbine_fatigue.pdf', 2, '2023-08-14', FALSE, NOW(), NOW());

-- PHYSICS (ID: 3)
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Muon g-2 Anomalies: A Path to New Physics?', 'Mohammed Salah', 'Recent measurements of the muon magnetic anomaly suggest a discrepancy with the Standard Model. This paper explores the possibility of a dark photon mediator explaining the 4.2-sigma deviation observed at Fermilab.', '/files/ph/muon_anomaly_study.pdf', 3, '2024-01-05', FALSE, NOW(), NOW()),
('High-Temperature Superconductivity in Hydrides', 'Oscar Fernandez', 'Observation of zero-resistance states in sulfur-hydride systems under pressures exceeding 150 GPa. While room temperature remains elusive, these findings pave the way for ambient-pressure applications via chemical pre-compression.', '/files/ph/supercond_hydride.pdf', 3, '2024-07-29', FALSE, NOW(), NOW()),
('Black Hole Information Paradox: A Firewall Perspective', 'Natalie Young', 'Does an observer falling into a black hole see a firewall? This theoretical analysis synthesizes the AMPS paradox with modern AdS/CFT correspondence theories to suggest that unitarity requires a radical rethinking of the event horizon.', '/files/ph/blackhole_firewall.pdf', 3, '2023-10-15', FALSE, NOW(), NOW()),
('Solar Flare Forecasting', 'Kevin Walker', 'Using X-ray flux data to predict M-class solar flares.', '/files/ph/solar_flares.pdf', 3, '2020-05-12', TRUE, '2020-05-12 11:00:00', NOW()),
('Neutrino Oscillation Parameters in Long-Baseline Experiments', 'Laura Palmer', 'Final results from the 5-year study on neutrino flavor mixing.', '/files/ph/neutrino_final.pdf', 3, '2024-12-01', FALSE, NOW(), NOW());

-- CHEMISTRY (ID: 4)
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Green Synthesis of Silver Nanoparticles using Eucalyptus Leaf Extract', 'Priya Singh, Anil Kapur', 'We report a cost-effective and eco-friendly method for the synthesis of silver nanoparticles (AgNPs). The plant metabolites act as both reducing and stabilizing agents. Characterization via UV-Vis and TEM confirms a spherical morphology with an average size of 20nm.', '/files/ch/green_nano_silver.pdf', 4, '2024-05-14', FALSE, NOW(), NOW()),
('Total Synthesis of Macrolide Antibiotics', 'Rachel Adams', 'An 18-step total synthesis of a novel macrolide.', '/files/ch/macrolide_synth.pdf', 4, '2023-02-10', TRUE, '2023-02-10 08:30:00', NOW()),
('Machine Learning in Retrosynthetic Analysis', 'Samuel Yeo', 'Can AI replace the chemist in route planning? We evaluate three transformer-based models on their ability to predict precursor molecules for complex natural products. Accuracy reached 78% for known reactions but dropped to 12% for novel bond-forming strategies.', '/files/ch/ml_retrosynthesis.pdf', 4, '2024-08-03', FALSE, NOW(), NOW()),
('Electrochemical Detection of Heavy Metals in Urban Waterways', 'Teresa van Dijk', 'Modified carbon paste electrodes for lead detection.', '/files/ch/heavy_metal_sensors.pdf', 4, '2024-10-10', FALSE, NOW(), NOW()),
('CO2 Capture via Metal-Organic Frameworks (MOFs)', 'Quentin Brooks', 'The climate crisis necessitates efficient carbon sequestration. This paper presents a novel MOF with an unprecedented surface area of 7000 m2/g, showing a 30% increase in CO2 adsorption capacity compared to HKUST-1.', '/files/ch/mof_carbon_capture.pdf', 4, '2024-12-15', FALSE, NOW(), NOW());

-- BUSINESS ADMINISTRATION (ID: 5)
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('The Gig Economy and Worker Well-being: A Longitudinal Study', 'Wendy Liu', 'As platform-based work becomes the norm, concerns regarding job security and mental health have surfaced. This three-year study of 2,000 Uber and DoorDash drivers suggests that while flexibility is valued, the lack of benefits leads to a 20% higher burnout rate compared to traditional employment.', '/files/ba/gig_economy_health.pdf', 5, '2024-04-11', FALSE, NOW(), NOW()),
('Corporate Social Responsibility or Greenwashing?', 'Victor Chang', 'Analysis of ESG reports from Fortune 500 companies.', '/files/ba/csr_vs_greenwashing.pdf', 5, '2023-09-30', FALSE, NOW(), NOW()),
('AI-Driven Dynamic Pricing in E-commerce', 'Uma Patel', 'How algorithms adjust prices in real-time.', '/files/ba/dynamic_pricing_v1.pdf', 5, '2022-01-15', TRUE, '2022-01-15 16:20:00', NOW()),
('The Impact of Remote Work on Organizational Culture', 'Xavier Perez, Yasmin Said', 'Is the "water cooler" conversation essential for innovation? Through a mixed-methods approach involving 15 tech firms, we found that while individual productivity rose by 10% during remote work, inter-departmental collaboration decreased by 25%.', '/files/ba/remote_work_culture.pdf', 5, '2024-06-25', FALSE, NOW(), NOW()),
('Venture Capital Trends in Southeast Asia', 'Lee Min-ho', 'A report on the shift from fintech to agritech investments in the ASEAN region.', '/files/ba/vc_trends_asean.pdf', 5, '2024-11-02', FALSE, NOW(), NOW());

-- 1. Computer Science: Neural Architecture
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Transformer-XL: Beyond Fixed-Length Context Windows in Natural Language Understanding', 'Dr. Aris Thorne, Sarah Jenkins',
'State-of-the-art language models are often limited by a fixed-length context window, which prevents the capture of long-range dependencies in extensive documents. This paper proposes a novel architecture that integrates a segment-level recurrence mechanism and a relative positional encoding scheme. Our evaluations on the WikiText-103 dataset demonstrate that our model achieves a perplexity of 18.3, outperforming standard Transformers by 15%. By reusing hidden states from previous segments, we enable the model to attend to information up to 450% further than traditional self-attention mechanisms. This advancement is critical for applications in long-form document summarization and legal tech analysis.',
'/files/cs/transformer_xl_deep_dive.pdf', 1, '2025-05-12', FALSE, NOW(), NOW());

-- 2. Mechanical Engineering: Fluid Dynamics
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Computational Fluid Dynamics Analysis of Drag Reduction in Heavy-Duty Vehicles', 'Marcus Vane, Linda Green',
'Aerodynamic drag accounts for approximately 65% of fuel consumption in Class 8 heavy-duty trucks at highway speeds. This study utilizes Reynolds-Averaged Navier-Stokes (RANS) simulations to evaluate the efficacy of active flow control systems, specifically synthetic jet actuators placed at the trailer tail. Computational results indicate that by manipulating the wake turbulence and delaying boundary layer separation, a net drag reduction of 12% is achievable. We provide a cost-benefit analysis comparing the energy expenditure of the actuators against the projected fuel savings of 4.5 gallons per 1,000 miles. These findings suggest that active aerodynamic surfaces are a viable alternative to passive fairings for long-haul logistics.',
'/files/me/cfd_truck_aerodynamics.pdf', 2, '2024-09-30', FALSE, NOW(), NOW());

-- 3. Physics: Quantum Information
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Error Mitigation Strategies in Noisy Intermediate-Scale Quantum (NISQ) Devices', 'Prof. Elena Rossi',
'The current generation of quantum processors is plagued by high decoherence rates and gate errors, necessitating robust error mitigation before full fault-tolerant computing is realized. This research focuses on Zero-Noise Extrapolation (ZNE) and Probabilistic Error Cancellation (PEC) techniques implemented on a 54-qubit superconducting processor. We demonstrate that by systematically scaling noise levels and extrapolating to the noiseless limit, we can improve the fidelity of VQE-based molecular simulations by up to 22%. However, the sampling overhead increases exponentially with the depth of the circuit. This paper proposes a hybrid mitigation-correction protocol that balances computational runtime with result accuracy for near-term quantum supremacy tasks.',
'/files/ph/quantum_error_mitigation.pdf', 3, '2025-11-04', FALSE, NOW(), NOW());

-- 4. Chemistry: Sustainable Catalysis
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Heterogeneous Catalysis for the Valorization of Lignocellulosic Biomass', 'Dr. Robert Moore, Jordan Bell',
'The transition to a bio-based economy requires the efficient conversion of non-edible biomass into platform chemicals. This paper details the development of a bifunctional ruthenium-based catalyst supported on activated carbon for the hydrogenolysis of lignin. Through a series of batch reactor experiments, we achieved a 72% yield of phenolic monomers at 250°C and 40 bar of H2 pressure. Kinetic studies reveal that the catalyst maintains high selectivity toward 4-propylguaiacol, with minimal carbon coking observed after five consecutive cycles. This research highlights a significant step toward replacing petroleum-derived aromatics with renewable alternatives derived from agricultural waste.',
'/files/ch/biomass_catalysis_vFinal.pdf', 4, '2024-06-18', FALSE, NOW(), NOW());

-- 5. Business Administration: Supply Chain/Risk
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Resilience Benchmarking in Global Supply Chains: A Post-Pandemic Analysis', 'Sofia Rodriguez, Michael Bloomberg Jr.',
'The unprecedented disruptions of the early 2020s exposed the fragility of "Just-in-Time" inventory systems. This study proposes a Multi-Criteria Decision-Making (MCDM) framework to quantify supply chain resilience across the electronics and pharmaceutical sectors. We analyze data from 120 global firms, measuring key performance indicators such as Time-to-Recover (TTR) and Time-to-Survive (TTS). Our findings indicate that firms with diversified geographic sourcing and high digital visibility experienced 40% shorter recovery times during port closures. The paper argues for a strategic shift toward "Just-in-Case" models and the integration of AI-driven predictive analytics to anticipate regional geopolitical risks.',
'/files/ba/supply_chain_resilience_2025.pdf', 5, '2023-11-20', TRUE, '2023-11-20 09:15:00', NOW());


-- 1. Computer Science: Distributed Systems & Consensus
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Adaptive Sharding Protocols for High-Throughput Distributed Ledgers', 'Dr. Katherine Singh, Liam O’Reilly',
'As blockchain networks scale toward global adoption, the trilemma of security, scalability, and decentralization remains a significant hurdle. This paper introduces "Aegis-Shard," a dynamic sharding protocol that reconfigures network partitions based on real-time transaction density and node churn. Unlike static sharding, which is vulnerable to 1% attacks on individual shards, Aegis-Shard utilizes a verifiable random function (VRF) to shuffle validators across shards every epoch. Our experimental results, conducted on a global testnet of 2,000 nodes, demonstrate a sustained throughput of 85,000 transactions per second (TPS) with sub-second finality. Furthermore, we analyze the cross-shard communication overhead and propose a "Receipt-Based" asynchronous atomic commit protocol that reduces inter-shard latency by 40%. The study concludes that adaptive sharding is essential for supporting CBDCs and high-frequency decentralized exchanges.',
'/files/cs/aegis_shard_ledger.pdf', 1, '2025-06-12', FALSE, NOW(), NOW());

-- 2. Mechanical Engineering: Advanced Manufacturing
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Influence of Laser Powder Bed Fusion (LPBF) Parameters on Porosity in Ti-6Al-4V', 'Jameson Burke, Dr. Sarah Miller',
'Additive manufacturing of titanium alloys like Ti-6Al-4V has revolutionized the production of custom medical implants and lightweight aerospace components. However, the formation of gas pores and "lack-of-fusion" voids remains a critical challenge for fatigue-sensitive applications. This research systematically investigates the relationship between laser power (150W–400W), scan speed (500mm/s–1500mm/s), and hatch spacing on the resultant densification. Through high-resolution X-ray computed tomography (XCT) and Archimedes density measurements, we identified a "conduction-mode" window that yields parts with >99.8% relative density. We also observe that excessive energy density leads to "keyhole" porosity, which significantly degrades the elongation-at-break from 12% to 4%. This paper provides a comprehensive process map for operators to optimize microstructure and mechanical properties during the LPBF process.',
'/files/me/titanium_lpbf_porosity.pdf', 2, '2025-02-18', FALSE, NOW(), NOW());

-- 3. Physics: Condensed Matter & Quantum Theory
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Topological Insulators and the Emergence of Majorana Fermions in Hybrid Nanowires', 'Prof. Viktor Volkov, Chen Ruo',
'The search for Majorana bound states (MBS) is driven by their potential application in topologically protected quantum computing. In this work, we report on transport measurements of InAs/Al hybrid nanowire devices under strong longitudinal magnetic fields. By tuning the chemical potential via a back-gate, we observed a zero-bias conductance peak (ZBCP) that persists over a significant range of magnetic fields and gate voltages, a hallmark signature of MBS. We contrast these findings with trivial Andreev bound states induced by disorder and provide a theoretical model based on the Bogoliubov-de Gennes equations to interpret the data. Our results suggest that while the ZBCP is a necessary indicator, future braiding experiments are required to confirm non-Abelian statistics. This research advances the understanding of topological phases in semiconductor-superconductor interfaces.',
'/files/ph/majorana_fermion_transport.pdf', 3, '2025-10-05', FALSE, NOW(), NOW());

-- 4. Chemistry: Atmospheric & Environmental Chemistry
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Photochemical Degradation of Tropospheric Ozone via Mineral Dust Aerosols', 'Dr. Elena Martinez, Omar Al-Zahrani',
'Mineral dust is one of the most abundant aerosol types in the atmosphere, yet its role as a reactive surface for trace gas depletion is often underrepresented in global climate models. This study investigates the heterogeneous uptake of ozone (O3) on various mineral proxies, including kaolinite, montmorillonite, and Saharan dust samples, using a Knudsen cell reactor. Our findings reveal that the uptake coefficient is highly dependent on relative humidity (RH), with a 50% decrease in reactivity as RH increases from 0% to 75% due to competitive water adsorption. Spectroscopic analysis via Diffuse Reflectance Infrared Fourier Transform (DRIFTS) identifies the formation of surface-bound superoxide and peroxide species as intermediate reaction products. This research suggests that dust plumes can significantly alter local ozone concentrations in arid regions, impacting both air quality and radiative forcing calculations.',
'/files/ch/ozone_dust_photochem.pdf', 4, '2024-11-30', FALSE, NOW(), NOW());

-- 5. Business Administration: Behavioral Economics
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Nudge Theory and Consumer Debt: A Field Experiment on Credit Card Repayment', 'Maya Gupta, Dr. Simon Wright',
'High-interest consumer debt remains a significant economic burden for millions of households. This paper presents the results of a large-scale randomized controlled trial (RCT) involving 15,000 credit card holders to test the efficacy of behavioral "nudges" in increasing monthly repayments. We implemented three treatment groups: a "Peer Comparison" nudge, a "Future Self" visualization, and an "Interest Salience" warning. Our analysis shows that the Interest Salience group—which received a clear breakdown of the long-term cost of making only minimum payments—increased their average monthly payment by 18.5% compared to the control group. Interestingly, the Peer Comparison nudge showed diminishing returns among high-income earners. This study provides empirical evidence for the use of choice architecture in financial services and offers policy recommendations for consumer protection bureaus to mandate more transparent disclosure formats.',
'/files/ba/nudge_theory_debt.pdf', 5, '2025-08-22', FALSE, NOW(), NOW());

-- 1. Computer Science: Artificial Intelligence & Ethics
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Mitigating Algorithmic Bias in Neural Ranking Models for Recruitment', 'Dr. Amara Okafor, Leo Schmidt',
'Automated recruitment systems often inherit historical biases present in training data, leading to the systemic exclusion of marginalized groups. This paper proposes a "Fair-Rank" framework that integrates a constrained optimization layer into the loss function of a Transformer-based ranker. By utilizing a demographic parity constraint during the fine-tuning phase on the LinkedIn-P dataset, we achieved a 40% reduction in disparate impact without significantly compromising the Normalized Discounted Cumulative Gain (NDCG). We further analyze the trade-off between model utility and fairness metrics, providing a Pareto-optimal frontier for HR-tech developers. The study concludes that adversarial debiasing is more effective than simple data re-sampling for long-tail distribution shifts in professional profiles.',
'/files/cs/fair_rank_bias_mitigation.pdf', 1, '2025-10-14', FALSE, NOW(), NOW());

-- 2. Mechanical Engineering: Fluid Dynamics & Aerospace
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Hypersonic Boundary Layer Transition on a Blunted Cone at Mach 10', 'Jean-Pierre Laurent, Dr. Sarah Miller',
'Predicting the transition from laminar to turbulent flow is critical for the thermal protection system (TPS) design of hypersonic re-entry vehicles. This research utilizes Direct Numerical Simulation (DNS) to investigate the evolution of Second-mode (Mack) instabilities on a 7-degree half-angle blunted cone. Our simulations, conducted on a 1.2-billion cell grid, reveal that nose bluntness exerts a stabilizing effect by thickening the shock layer and modifying the entropy gradient. We present a modified e^N transition model that accounts for the bluntness-induced damping of high-frequency acoustic waves. Comparisons with experimental wind tunnel data at Mach 10 show an 85% correlation in the predicted transition location. This work provides high-fidelity data for the next generation of scramjet-powered vehicle design.',
'/files/me/hypersonic_mach10_dns.pdf', 2, '2025-04-05', FALSE, NOW(), NOW());

-- 3. Physics: Particle Physics & Neutrino Detection
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Background Characterization for Liquid Argon Time Projection Chambers (LArTPC)', 'Hiroki Sato, Dr. Elena Rossi',
'Deep-underground neutrino experiments, such as DUNE, rely on Liquid Argon Time Projection Chambers (LArTPC) to detect rare neutrino interactions with unprecedented spatial resolution. However, cosmic-ray induced neutrons and radioactive isotopes like Argon-39 present significant background challenges. This paper details a 12-month background study conducted at the Sanford Underground Research Facility (SURF). We developed a machine-learning-based pulse-shape discrimination (PSD) algorithm to differentiate between electronic and nuclear recoils. Our results demonstrate a rejection power of 10^7 for beta particles while maintaining 95% efficiency for neutrino signal candidates. We also provide a comprehensive map of the thermal neutron flux within the cryostat, which is essential for future searches for supernova burst neutrinos and proton decay.',
'/files/ph/lartpc_background_dune.pdf', 3, '2025-01-22', FALSE, NOW(), NOW());

-- 4. Chemistry: Analytical Chemistry & Nanotech
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Single-Molecule SERS Detection using Plasmonic Nanoparticle-on-Mirror Geometries', 'Dr. Robert Moore, Isabella Conti',
'Surface-Enhanced Raman Spectroscopy (SERS) has reached the ultimate limit of analytical sensitivity, enabling the detection of individual molecules. This study utilizes a "Nanoparticle-on-Mirror" (NPoM) architecture, where gold nanospheres are separated from a flat gold film by a 1.2 nm thick self-assembled monolayer (SAM). This geometry creates a sub-nanometer "hotspot" where the local electromagnetic field enhancement factor exceeds 10^10. By monitoring the fluctuations of the 1640 cm^-1 vibrational mode of Rhodamine 6G, we demonstrate the capture and release of single molecules within the plasmonic gap. We analyze the spectral "blinking" and line-broadening effects as a function of incident laser power. This research paves the way for ultra-sensitive point-of-care diagnostics and the study of molecular electronics in real-time.',
'/files/ch/sers_single_molecule_npom.pdf', 4, '2025-09-02', FALSE, NOW(), NOW());

-- 5. Business Administration: Fintech & Game Theory
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Equilibrium Analysis of Automated Market Makers (AMM) in Decentralized Finance', 'Xavier Perez, Dr. Simon Wright',
'Automated Market Makers (AMMs), such as Uniswap, have redefined liquidity provision by replacing order books with mathematical bonding curves. This paper explores the game-theoretic equilibrium between liquidity providers (LPs) and arbitrageurs in a Constant Product Market Maker (CPMM) model. We derive a closed-form solution for "Impermanent Loss" (IL) under stochastic price volatility and propose a dynamic fee-switching mechanism to hedge against LVR (Loss-Versus-Rebalancing). Using historical data from Ethereum-based DEXs, we show that our proposed dynamic fee model increases LP profitability by 15% during high-volatility regimes without reducing total trading volume. The paper concludes with an analysis of MEV (Maximal Extractable Value) and its impact on price slippage for retail participants.',
'/files/ba/amm_equilibrium_defi.pdf', 5, '2025-12-01', FALSE, NOW(), NOW());
