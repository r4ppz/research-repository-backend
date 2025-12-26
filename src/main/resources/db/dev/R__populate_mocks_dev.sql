-- Departments
INSERT INTO departments (department_name)
VALUES
  ('Computer Science'),
  ('Mechanical Engineering'),
  ('Physics'),
  ('Chemistry'),
  ('Business Administration');

-- Computer Science
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Efficient Algorithms for Large-Scale Graph Processing', 'Alice Smith', 'This paper explores new algorithmic techniques for processing massive graphs efficiently in distributed systems.', '/files/cs_graphs.pdf', (SELECT department_id FROM departments WHERE department_name='Computer Science'), '2024-04-01', FALSE, NOW(), NOW()),
('Deep Learning for Medical Diagnosis', 'Bob Zhang', 'We present a novel deep neural network for predictive diagnosis based on medical images and EHR data.', '/files/cs_dl_medical.pdf', (SELECT department_id FROM departments WHERE department_name='Computer Science'), '2024-06-15', FALSE, NOW(), NOW()),
('Privacy-Preserving Data Mining Techniques', 'Carol Johnson', 'This paper evaluates techniques that allow data mining while maintaining user privacy, especially in healthcare domains.', '/files/cs_privacy.pdf', (SELECT department_id FROM departments WHERE department_name='Computer Science'), '2023-12-01', FALSE, NOW(), NOW()),
('Natural Language Processing in Low-Resource Languages', 'David Kim', 'The challenges and solutions for NLP tasks in languages lacking large datasets are analyzed with empirical results.', '/files/cs_nlp.pdf', (SELECT department_id FROM departments WHERE department_name='Computer Science'), '2024-08-21', FALSE, NOW(), NOW()),
('Quantum Computing and Cryptography', 'Emma Turner', 'Emerging quantum algorithms threaten standard cryptographic schemes; post-quantum alternatives are assessed.', '/files/cs_quantum.pdf', (SELECT department_id FROM departments WHERE department_name='Computer Science'), '2024-01-15', FALSE, NOW(), NOW());

-- Mechanical Engineering
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Advances in Renewable Energy Systems', 'Frank Miller', 'A survey of recent advances in solar and wind power integration for large-scale energy grids.', '/files/me_renewable.pdf', (SELECT department_id FROM departments WHERE department_name='Mechanical Engineering'), '2023-09-20', FALSE, NOW(), NOW()),
('Robotics: Autonomous Mobile Systems', 'Grace Lee', 'Design architectures and sensor fusion methods for truly autonomous mobile robotic systems.', '/files/me_robotics.pdf', (SELECT department_id FROM departments WHERE department_name='Mechanical Engineering'), '2024-03-12', FALSE, NOW(), NOW()),
('Thermodynamic Analysis of Jet Engines', 'Hiro Tanaka', 'A comparative thermodynamic analysis of next-generation jet engines for commercial flight.', '/files/me_jetengines.pdf', (SELECT department_id FROM departments WHERE department_name='Mechanical Engineering'), '2024-05-05', FALSE, NOW(), NOW()),
('Materials Science: Smart Alloys', 'Ishaan Kumar', 'Examines the properties and applications of shape-memory alloys in micro-actuation.', '/files/me_alloys.pdf', (SELECT department_id FROM departments WHERE department_name='Mechanical Engineering'), '2024-07-30', FALSE, NOW(), NOW()),
('Biomechanics in Injury Prevention', 'Julia Morgan', 'Modeling human biomechanics to inform improved safety equipment design for athletes.', '/files/me_biomechanics.pdf', (SELECT department_id FROM departments WHERE department_name='Mechanical Engineering'), '2023-11-18', FALSE, NOW(), NOW());

-- Physics
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Dark Matter: New Experimental Approaches', 'Kevin Walker', 'Updates on dark matter detection methodologies using cryogenic and underground facilities.', '/files/ph_darkmatter.pdf', (SELECT department_id FROM departments WHERE department_name='Physics'), '2024-02-28', FALSE, NOW(), NOW()),
('Quantum Entanglement and Communication', 'Lauren Hill', 'Exploring quantum entanglement applications in secure satellite communication.', '/files/ph_entanglement.pdf', (SELECT department_id FROM departments WHERE department_name='Physics'), '2024-06-02', FALSE, NOW(), NOW()),
('Particle Physics: Higgs Boson Insights', 'Mohammed Salah', 'Analysis of recent LHC data on the properties and decay channels of Higgs boson particles.', '/files/ph_higgs.pdf', (SELECT department_id FROM departments WHERE department_name='Physics'), '2024-04-20', FALSE, NOW(), NOW()),
('Astrophysics: Gravitational Waves', 'Natalie Young', 'Discusses new findings from LIGO and Virgo collaborations on gravitational wave observation.', '/files/ph_gravwaves.pdf', (SELECT department_id FROM departments WHERE department_name='Physics'), '2023-12-10', FALSE, NOW(), NOW()),
('Superconductivity in Novel Materials', 'Oscar Fernandez', 'Investigates unconventional superconductivity in newly discovered 2D materials at low temperatures.', '/files/ph_superconductivity.pdf', (SELECT department_id FROM departments WHERE department_name='Physics'), '2024-07-14', FALSE, NOW(), NOW());

-- Chemistry
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Organic Synthesis for Pharmaceuticals', 'Priya Singh', 'Develops new methods for enantioselective synthesis in pharma compounds.', '/files/ch_organicsynth.pdf', (SELECT department_id FROM departments WHERE department_name='Chemistry'), '2024-03-26', FALSE, NOW(), NOW()),
('Catalyst Design in Green Chemistry', 'Quentin Brooks', 'Presents recent work in catalyst engineering for more sustainable chemical processes.', '/files/ch_catalyst.pdf', (SELECT department_id FROM departments WHERE department_name='Chemistry'), '2023-11-03', FALSE, NOW(), NOW()),
('Surface Chemistry of Nanomaterials', 'Rachel Adams', 'Examines the effects of surface functionalization in nanomaterials applications.', '/files/ch_nanomaterials.pdf', (SELECT department_id FROM departments WHERE department_name='Chemistry'), '2024-05-27', FALSE, NOW(), NOW()),
('Chemical Sensors for Biomedical Use', 'Samuel Yeo', 'Discusses development of selective chemical sensors for medical diagnostics.', '/files/ch_sensors.pdf', (SELECT department_id FROM departments WHERE department_name='Chemistry'), '2024-06-10', FALSE, NOW(), NOW()),
('Spectroscopy: Understanding Molecular Structures', 'Teresa van Dijk', 'Utilizes advanced spectroscopy to determine complex organic molecular structures.', '/files/ch_spectroscopy.pdf', (SELECT department_id FROM departments WHERE department_name='Chemistry'), '2024-07-22', FALSE, NOW(), NOW());

-- Business Administration
INSERT INTO research_papers (title, author_name, abstract_text, file_path, department_id, submission_date, archived, created_at, updated_at)
VALUES
('Digital Marketing Analytics', 'Uma Patel', 'Evaluates the impact of modern analytics tools on strategic digital marketing decisions.', '/files/ba_marketing.pdf', (SELECT department_id FROM departments WHERE department_name='Business Administration'), '2024-04-17', FALSE, NOW(), NOW()),
('Financial Technology Adoption in SMEs', 'Victor Chang', 'Reports on SME adoption of fintech platforms and resulting performance impacts.', '/files/ba_fintech.pdf', (SELECT department_id FROM departments WHERE department_name='Business Administration'), '2024-03-07', FALSE, NOW(), NOW()),
('Leadership Styles in Remote Teams', 'Wendy Liu', 'Surveys how leadership approaches affect productivity in distributed teams.', '/files/ba_leadership.pdf', (SELECT department_id FROM departments WHERE department_name='Business Administration'), '2023-12-19', FALSE, NOW(), NOW()),
('Supply Chain Optimization with AI', 'Xavier Perez', 'Proposes machine-learning models to improve efficiency in global supply chains.', '/files/ba_supplychain.pdf', (SELECT department_id FROM departments WHERE department_name='Business Administration'), '2024-01-08', FALSE, NOW(), NOW()),
('Consumer Behavior Trends Post-COVID', 'Yasmin Said', 'Analyzes changing patterns in consumer spending and preferences since the pandemic.', '/files/ba_consumer.pdf', (SELECT department_id FROM departments WHERE department_name='Business Administration'), '2024-05-11', FALSE, NOW(), NOW());
